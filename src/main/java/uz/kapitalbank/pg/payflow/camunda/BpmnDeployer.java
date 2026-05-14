package uz.kapitalbank.pg.payflow.camunda;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BpmnDeployer {

  RestClient restClient;
  ResourcePatternResolver resourcePatternResolver;

  @EventListener(ApplicationReadyEvent.class)
  public void deploy() {
    try {
      Resource[] files = resourcePatternResolver.getResources("classpath:bpmn/*.bpmn");
      log.info("{} files found and will deploy", files.length);

      for (Resource bpmn : files) {
        deployOne(bpmn);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void deployOne(Resource bpmn) {
    try {
      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add("deployment-name", "payflow_worker");
      body.add("enable-duplicate-filtering", "true");
      body.add("deploy-changed-only", "true");

      InputStreamResource fileResource = new InputStreamResource(bpmn.getInputStream()) {
        @Override
        public String getFilename() {
          return bpmn.getFilename();
        }

        @Override
        public long contentLength() throws IOException {
          return bpmn.contentLength();
        }
      };
      body.add((bpmn.getFilename()), fileResource);

      restClient.post()
        .uri("/deployment/create")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(body)
        .retrieve()
        .toBodilessEntity();

      log.info("Deployed BPMN: {}", bpmn.getFilename());
    } catch (Exception e) {
      log.error("Failed to deploy {}", bpmn.getFilename(), e);
    }
  }
}