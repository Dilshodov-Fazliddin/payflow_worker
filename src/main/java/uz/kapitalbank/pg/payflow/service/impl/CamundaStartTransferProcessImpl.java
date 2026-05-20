package uz.kapitalbank.pg.payflow.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uz.kapitalbank.pg.payflow.dto.request.TransferToProcess;
import uz.kapitalbank.pg.payflow.dto.response.StartTransferResponse;
import uz.kapitalbank.pg.payflow.exception.ProcessStartException;
import uz.kapitalbank.pg.payflow.service.CamundaStartTransferProcess;


import java.util.HashMap;
import java.util.Map;

import static uz.kapitalbank.pg.payflow.camunda.constant.CamundaConstants.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CamundaStartTransferProcessImpl implements CamundaStartTransferProcess {

  private static final String PROCESS_KEY = "paymentProcess";

  RestClient camundaRestClient;

  @Override
  public StartTransferResponse startTransfer(TransferToProcess request) {
    Map<String, Object> variables = new HashMap<>();
    variables.put(FROM_ACCOUNT, camundaVar(request.getFromAccount(), "Long"));
    variables.put(TO_ACCOUNT, camundaVar(request.getToAccount(), "Long"));
    variables.put(AMOUNT, camundaVar(request.getAmount(), "Long"));
    variables.put(TRANSFER_ID, camundaVar(request.getTransferId(), "Long"));
    variables.put(FRAUD_CHECK_PASSED, camundaVar(request.getFraudCheckPassed(), "Boolean"));


    Map<String, Object> body = Map.of(
      "variables", variables,
      "businessKey", String.valueOf(request.getTransferId())
    );

    try {
      JsonNode response = camundaRestClient.post()
        .uri("/process-definition/key/{key}/start", PROCESS_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .retrieve()
        .body(JsonNode.class);

      String processInstanceId = response.get("id").asText();
      log.info("Started process: pid={}, transferId={}",
        processInstanceId, request.getTransferId());

      return new StartTransferResponse(
        processInstanceId,
        request.getTransferId(),
        "PENDING"
      );

    } catch (Exception e) {
      log.error("Failed to start payment process for transferId={}",
        request.getTransferId(), e);
      throw new ProcessStartException(
        "Failed to start payment process: " + e.getMessage(), e);
    }
  }

  private Map<String, Object> camundaVar(Object value, String type) {
    Map<String, Object> v = new HashMap<>();
    v.put("value", value);
    v.put("type", type);
    return v;
  }
}