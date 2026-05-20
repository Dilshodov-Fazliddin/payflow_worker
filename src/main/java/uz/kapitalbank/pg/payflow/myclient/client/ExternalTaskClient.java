package uz.kapitalbank.pg.payflow.myclient.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uz.kapitalbank.pg.payflow.myclient.builder.ExternalTaskBuilder;
import uz.kapitalbank.pg.payflow.myclient.handler.ExternalTaskHandler;
import uz.kapitalbank.pg.payflow.myclient.service.ExternalTaskService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExternalTaskClient {

  private static final Logger log = LoggerFactory.getLogger(ExternalTaskClient.class);

  private final String baseUrl;
  private final String workerId;
  private final int maxTasks;
  private final long asyncResponseTimeout;
  private final String authHeader;
  private final HttpClient http;
  private final ObjectMapper mapper;
  private final ExternalTaskService service;
  private final String username;
  private  final String password;

  private final Map<String, HandlerEntry> handlers = new ConcurrentHashMap<>();
  private final AtomicBoolean running = new AtomicBoolean(false);

  private ExecutorService pollerExecutor;
  private ExecutorService workerExecutor;

  public ExternalTaskClient(String baseUrl,
                            String workerId,
                            int maxTasks,
                            long asyncResponseTimeout, String authHeader, String username, String password) {
    this.baseUrl = baseUrl;
    this.workerId = workerId;
    this.maxTasks = maxTasks;
    this.asyncResponseTimeout = asyncResponseTimeout;
    this.username = username;
    this.password = password;
    this.http = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(10))
      .build();
    this.mapper = new ObjectMapper();
    this.authHeader= "Basic " + Base64.getEncoder()
      .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    this.service = new ExternalTaskService(http, mapper, baseUrl, workerId,username,password);
  }

  public void subscribe(String topicName,
                        long lockDuration,
                        String[] variableNames,
                        ExternalTaskHandler handler) {
    handlers.put(topicName, new HandlerEntry(topicName, lockDuration, variableNames, handler));
    log.info("Подписка на топик: {} (lockDuration={}ms, variables={})",
      topicName, lockDuration,
      variableNames == null || variableNames.length == 0 ? "ALL" : Arrays.toString(variableNames));
  }

  public void start() {
    if (!running.compareAndSet(false, true)) return;
    if (handlers.isEmpty()) {
      log.warn("Стартую клиент, но нет ни одного хендлера!");
    }
    pollerExecutor = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "camunda-poller");
      t.setDaemon(true);
      return t;
    });
    workerExecutor = Executors.newFixedThreadPool(Math.max(2, maxTasks), r -> {
      Thread t = new Thread(r, "camunda-worker");
      t.setDaemon(true);
      return t;
    });
    pollerExecutor.submit(this::pollLoop);
    log.info("ExternalTaskClient запущен, workerId={}, baseUrl={}", workerId, baseUrl);
  }

  public void stop() {
    if (!running.compareAndSet(true, false)) return;
    if (pollerExecutor != null) pollerExecutor.shutdownNow();
    if (workerExecutor != null) workerExecutor.shutdown();
    log.info("ExternalTaskClient остановлен");
  }


  private void pollLoop() {
    while (running.get()) {
      try {
        List<ExternalTaskBuilder> tasks = fetchAndLock();
        for (ExternalTaskBuilder task : tasks) {
          workerExecutor.submit(() -> handleTask(task));
        }
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
        break;
      } catch (Exception e) {
        log.error("Ошибка при поллинге", e);
        sleep(2000);
      }
    }
  }

  private List<ExternalTaskBuilder> fetchAndLock() throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("workerId", workerId);
    body.put("maxTasks", maxTasks);
    body.put("asyncResponseTimeout", asyncResponseTimeout);

    List<Map<String, Object>> topics = new ArrayList<>();
    for (HandlerEntry h : handlers.values()) {
      Map<String, Object> t = new HashMap<>();
      t.put("topicName", h.topicName);
      t.put("lockDuration", h.lockDuration);
      if (h.variableNames != null && h.variableNames.length > 0) {
        t.put("variables", Arrays.asList(h.variableNames));
      }
      topics.add(t);
    }
    body.put("topics", topics);

    HttpRequest req = HttpRequest.newBuilder()
      .uri(URI.create(baseUrl + "/external-task/fetchAndLock"))
      .header("Content-Type", "application/json")
      .header("Authorization", authHeader)
      .timeout(Duration.ofMillis(asyncResponseTimeout + 5000))
      .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
      .build();

    HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
    if (resp.statusCode() >= 300) {
      throw new RuntimeException("fetchAndLock -> " + resp.statusCode() + ": " + resp.body());
    }
    ExternalTaskBuilder[] arr = mapper.readValue(resp.body(), ExternalTaskBuilder[].class);
    return Arrays.asList(arr);
  }

  private void handleTask(ExternalTaskBuilder task) {
    HandlerEntry entry = handlers.get(task.getTopicName());
    if (entry == null) {
      log.warn("Нет хендлера для топика: {}", task.getTopicName());
      return;
    }
    try {
      entry.handler.execute(task, service);
    } catch (Exception e) {
      log.error("Ошибка в хендлере, taskId={}", task.getId(), e);
      try {
        service.handleFailure(task, e.getMessage(), stackTrace(e), 0, 0);
      } catch (Exception suppressed) {
        log.error("Не смог отправить failure", suppressed);
      }
    }
  }

  private String stackTrace(Throwable t) {
    StringBuilder sb = new StringBuilder();
    sb.append(t).append('\n');
    for (StackTraceElement el : t.getStackTrace()) sb.append("  at ").append(el).append('\n');
    return sb.toString();
  }

  private void sleep(long ms) {
    try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
  }

  private static class HandlerEntry {
    final String topicName;
    final long lockDuration;
    final String[] variableNames;
    final ExternalTaskHandler handler;

    HandlerEntry(String topicName, long lockDuration, String[] variableNames, ExternalTaskHandler handler) {
      this.topicName = topicName;
      this.lockDuration = lockDuration;
      this.variableNames = variableNames;
      this.handler = handler;
    }
  }
}