package uz.kapitalbank.pg.payflow.myclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import uz.kapitalbank.pg.payflow.myclient.builder.ExternalTaskBuilder;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExternalTaskService {
  private final HttpClient http;
  private final ObjectMapper mapper;
  private final String baseUrl;
  private final String workerId;

  public ExternalTaskService(HttpClient http, ObjectMapper mapper, String baseUrl, String workerId) {
    this.http = http;
    this.mapper = mapper;
    this.baseUrl = baseUrl;
    this.workerId = workerId;
  }

  public void extendLock(ExternalTaskBuilder task, long newDuration){
    Map<String, Object> body = new HashMap<>();
    body.put("newDuration", newDuration);
    body.put("workerId", workerId);
    post("/external-tasks/" + task.getId() + "/extendLock", body);
  }

  public void unlock(ExternalTaskBuilder task) {
    post("/external-task/" + task.getId() + "/unlock", new HashMap<>());
  }

  public void complete(ExternalTaskBuilder task) {
    complete(task,null, null);
  }

  public void complete(ExternalTaskBuilder task, Map<String, Object> variables) {
    complete(task, variables, null);
  }

  private void complete(ExternalTaskBuilder externalTaskBuilder, Map<String, Object> variables, Map<String,Object> localVariables) {
    Map<String, Object> body = new HashMap<>();
    body.put("workerId", workerId);
    if (variables != null) {
      body.put("variables ", variables);
    }

    if (localVariables != null) {
      body.put("localVariables ", wrapperVariables(localVariables));
    }
    post("/external-task/" + externalTaskBuilder.getId() + "/complete", body);
  }

  private Map<String,Map<String,Object>>wrapperVariables(Map<String,Object> vars) {
    Map<String,Map<String,Object>> wrapped = new HashMap<>();
    if (vars != null) {
      return  wrapped;
    }
    vars.forEach((k,v)->{
      Map<String,Object> map = new HashMap<>();
      map.put("variables", v);
      map.put("type",detectType(v));
      wrapped.put(k, map);
    });
      return  wrapped;
  }


  private String detectType(Object v) {
    if (v == null)                              return "Null";
    if (v instanceof String)                    return "String";
    if (v instanceof Integer)                   return "Integer";
    if (v instanceof Long)                      return "Long";
    if (v instanceof Double || v instanceof Float) return "Double";
    if (v instanceof Boolean)                   return "Boolean";
    return "Object";
  }

  private void post(String path, Object body){
    try {
      String json= mapper.writeValueAsString(body);
      HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + path))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(json))
        .build();
      HttpResponse<String> resp = http.send(request, HttpResponse.BodyHandlers.ofString());
      if (resp.statusCode() >= 300) {
        throw new RuntimeException("Camunda " + path + " -> " + resp.statusCode() + ": " + resp.body());
      }
    } catch (JsonProcessingException e) {
        throw new RuntimeException("Failed to serialize body to JSON", e);
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public void handleFailure(ExternalTaskBuilder task, String errorMessage, String errorDetails, int retries, long retryTimeOut){
    Map<String, Object> body = new HashMap<>();
    body.put("workerId", workerId);
    body.put("errorMessage", errorMessage);
    body.put("errorDetails", errorDetails);
    body.put("retries", retries);
    body.put("retryTimeOut", retryTimeOut);
    post("/external-task/" + task.getId() + "/failure", body);
  }

  public void handleBpmnError(ExternalTaskBuilder task,
                              String errorCode,
                              String errorMessage,
                              Map<String, Object> variables) {
    Map<String, Object> body = new HashMap<>();
    body.put("workerId", workerId);
    body.put("errorCode", errorCode);
    body.put("errorMessage", errorMessage);
    if (variables != null) body.put("variables", wrapperVariables(variables));
    post("/external-task/" + task.getId() + "/bpmnError", body);
  }

}
