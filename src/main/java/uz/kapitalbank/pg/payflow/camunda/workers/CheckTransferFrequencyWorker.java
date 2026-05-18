package uz.kapitalbank.pg.payflow.camunda.workers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import uz.kapitalbank.pg.payflow.camunda.constant.CamundaConstants;
import uz.kapitalbank.pg.payflow.constant.enums.FraudDecision;
import uz.kapitalbank.pg.payflow.dto.response.FraudCheckResult;
import uz.kapitalbank.pg.payflow.myclient.builder.ExternalTaskBuilder;
import uz.kapitalbank.pg.payflow.myclient.handler.ExternalTaskHandler;
import uz.kapitalbank.pg.payflow.myclient.service.ExternalTaskService;
import uz.kapitalbank.pg.payflow.myclient.subscription.ExternalTaskSubscription;
import uz.kapitalbank.pg.payflow.service.TransferService;
import uz.kapitalbank.pg.payflow.service.impl.FraudCheckService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Map;

import static uz.kapitalbank.pg.payflow.camunda.constant.CamundaConstants.*;

@Component
@ExternalTaskSubscription(
  topicName = CHECK_FREQUENCY,
  lockDuration = 30000,
  variableNames = {FROM_ACCOUNT, AMOUNT, TRANSFER_ID}
)
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CheckTransferFrequencyWorker implements ExternalTaskHandler {

  FraudCheckService fraudCheckService;
  TransferService transferService;
  @Override
  public void execute(ExternalTaskBuilder task, ExternalTaskService taskService) {
    MDC.put("processInstanceId", task.getProcessInstanceId());
    log.info("FraudCheck task received: id={}", task.getId());

    try {
      Long fromAccountId = task.getVariable(FROM_ACCOUNT);
      Long amountRaw = task.getVariable(AMOUNT);
      BigDecimal amount = BigDecimal.valueOf(amountRaw);

      FraudCheckResult result = fraudCheckService.check(fromAccountId, amount);
      log.info("Fraud decision: {}", result.getDecision());

      taskService.complete(task, Map.of(
        FRAUD_CHECK_PASSED, result.getDecision().equals(FraudDecision.APPROVED),
        "fraudDecision",        result.getDecision().name(),
        "fraudReason",          result.getReason(),
        "recentTransferCounts", result.getRecentCount(),
        "fraudPassed", true
      ));

    } catch (Exception e) {
      transferService.markAsFailed(task.getVariable(TRANSFER_ID));
      log.error("FraudCheck failed with technical error", e);
      int retries = task.getRetries() == null ? 3 : task.getRetries() - 1;
      taskService.handleFailure(
        task,
        e.getMessage(),
        stackTraceToString(e),
        retries,
        60_000L
      );
    } finally {
      MDC.clear();
    }
  }

  private String stackTraceToString(Throwable t) {
    StringWriter sw = new StringWriter();
    t.printStackTrace(new PrintWriter(sw));
    return sw.toString();
  }
}