package uz.kapitalbank.pg.payflow.myclient.configuration;

import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uz.kapitalbank.pg.payflow.myclient.client.ExternalTaskClient;
import uz.kapitalbank.pg.payflow.myclient.handler.ExternalTaskHandler;
import uz.kapitalbank.pg.payflow.myclient.subscription.ExternalTaskSubscription;

import java.util.Map;


@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExternalTasClientConfig {
  static final Logger logger = LoggerFactory.getLogger(ExternalTasClientConfig.class);
  @Value("${camunda.bpm.client.base-url}")
  private String baseUrl;

  @Value("${camunda.bpm.client.worker-id}")
  private String workerId;

  @Value("${camunda.bpm.client.max-tasks}")
  private int maxTasks;

  @Value("${camunda.bpm.client.async-response-timeout}")
  private long asyncResponseTimeout;
  @Value("${camunda.bpm.client.basic-auth.username}")
  private String username;
  @Value("${camunda.engine.password}")
  private String password;
  private String authHeader;

  private ExternalTaskClient client;

  @Bean
  public ExternalTaskClient externalTaskClient(ApplicationContext context) {
    client = new ExternalTaskClient(baseUrl, workerId, maxTasks, asyncResponseTimeout, authHeader, username, password);

    Map<String, Object> beans = context.getBeansWithAnnotation(ExternalTaskSubscription.class);
    for (Object bean : beans.values()) {
      if (!(bean instanceof ExternalTaskHandler)) {
        logger.warn("Бин {} помечен @ExternalTaskSubscription, но не реализует ExternalTaskHandler — пропускаю",
          bean.getClass().getName());
        continue;
      }
      ExternalTaskSubscription ann = bean.getClass().getAnnotation(ExternalTaskSubscription.class);

      client.subscribe(ann.topicName(), ann.lockDuration(), ann.variableNames(), (ExternalTaskHandler) bean);
    }

    client.start();
    return client;
  }

  @PreDestroy
  public void stop() {
    if (client != null) client.stop();
  }
}
