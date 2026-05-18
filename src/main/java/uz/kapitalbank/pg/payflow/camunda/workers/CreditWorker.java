package uz.kapitalbank.pg.payflow.camunda.workers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import uz.kapitalbank.pg.payflow.constant.enums.TransferStatus;
import uz.kapitalbank.pg.payflow.exception.DataNotFoundException;
import uz.kapitalbank.pg.payflow.exception.TransferCanceledException;
import uz.kapitalbank.pg.payflow.myclient.builder.ExternalTaskBuilder;
import uz.kapitalbank.pg.payflow.myclient.handler.ExternalTaskHandler;
import uz.kapitalbank.pg.payflow.myclient.service.ExternalTaskService;
import uz.kapitalbank.pg.payflow.myclient.subscription.ExternalTaskSubscription;
import uz.kapitalbank.pg.payflow.service.TransferService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static uz.kapitalbank.pg.payflow.camunda.constant.CamundaConstants.AMOUNT;
import static uz.kapitalbank.pg.payflow.camunda.constant.CamundaConstants.CREDIT_WORKER;
import static uz.kapitalbank.pg.payflow.camunda.constant.CamundaConstants.TO_ACCOUNT;
import static uz.kapitalbank.pg.payflow.camunda.constant.CamundaConstants.TRANSFER_ID;

@Component
@RequiredArgsConstructor
@ExternalTaskSubscription(
  topicName = CREDIT_WORKER,
  lockDuration = 30000,
  variableNames = {TO_ACCOUNT, AMOUNT,TRANSFER_ID}
)
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CreditWorker implements ExternalTaskHandler {
  TransferService transferService;

  @Override
  public void execute(ExternalTaskBuilder task, ExternalTaskService service) throws Exception {
    MDC.put("processInstanceId", task.getProcessInstanceId());
    log.info("FraudCheck task received: id={}", task.getId());

    try {
      Long toAccount = task.getVariable(TO_ACCOUNT);
      Long amount = task.getVariable(AMOUNT);
      Long transferId = task.getVariable(TRANSFER_ID);

      try {
        transferService.credit(toAccount, amount, transferId);
      } catch (TransferCanceledException e) {
        log.warn("We can't initialize your transaction: toAccount={}", toAccount);
        service.handleBpmnError(task, "CREDIT_ERROR", e.getMessage(), null);
        return;
      }
      transferService.changeTransferStatus(transferId, TransferStatus.COMPLETED);
      service.complete(task, Map.of("CreditPass",true));
      log.info("CREDIT SUCCEEDED: id={}", toAccount);
    } catch (DataNotFoundException e) {
      log.warn("Task {} no longer exists", task.getId());
    } catch (Exception e) {
      transferService.markAsFailed(task.getVariable(TRANSFER_ID));
      int retries = task.getRetries() == null ? 3 : task.getRetries() - 1;
      try {
        service.handleFailure(
          task,
          e.getMessage(),
          stackTraceToString(e),
          retries,
          60_000L
        );
      }catch (DataNotFoundException nfe){
        log.warn("Could not report failure — task gone");
      }finally {
        MDC.clear();
      }
    }
  }
  private String stackTraceToString(Throwable t) {
    StringWriter sw = new StringWriter();
    t.printStackTrace(new PrintWriter(sw));
    return sw.toString();
  }
}
