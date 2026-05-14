package uz.kapitalbank.pg.payflow.camunda;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CamundaRestConfig {

  @Bean
  public RestClient camundaRestClient(
    @Value("${camunda.engine.base-url}") String baseUrl,
    @Value("${camunda.engine.username}") String username,
    @Value("${camunda.engine.password}") String password
  ) {
    return RestClient.builder()
      .baseUrl(baseUrl)
      .defaultHeaders(headers -> headers.setBasicAuth(username, password))
      .build();
  }
}
