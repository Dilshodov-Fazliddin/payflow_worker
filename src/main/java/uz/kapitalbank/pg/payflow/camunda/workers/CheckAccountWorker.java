package uz.kapitalbank.pg.payflow.camunda.workers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.kapitalbank.pg.payflow.exception.DataNotFoundException;
import uz.kapitalbank.pg.payflow.myclient.builder.ExternalTaskBuilder;
import uz.kapitalbank.pg.payflow.myclient.handler.ExternalTaskHandler;
import uz.kapitalbank.pg.payflow.myclient.service.ExternalTaskService;
import uz.kapitalbank.pg.payflow.myclient.subscription.ExternalTaskSubscription;
import uz.kapitalbank.pg.payflow.service.AccountService;
import uz.kapitalbank.pg.payflow.service.TransferService;

import static uz.kapitalbank.pg.payflow.camunda.constant.CamundaConstants.CHECK_ACCOUNT_TOPIC;
import static uz.kapitalbank.pg.payflow.camunda.constant.CamundaConstants.FROM_ACCOUNT;
import static uz.kapitalbank.pg.payflow.camunda.constant.CamundaConstants.TO_ACCOUNT;
import static uz.kapitalbank.pg.payflow.camunda.constant.CamundaConstants.TRANSFER_ID;

@Component
@ExternalTaskSubscription(
  topicName = CHECK_ACCOUNT_TOPIC,
  lockDuration = 30000,
  variableNames = {FROM_ACCOUNT, TO_ACCOUNT, TRANSFER_ID}
)
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CheckAccountWorker implements ExternalTaskHandler {

  AccountService accountService;
  TransferService transferService;

  @Override
  public void execute(ExternalTaskBuilder externalTask, ExternalTaskService externalTaskService) {
    log.info("Received task: id={}, processInstanceId={}",
      externalTask.getId(), externalTask.getProcessInstanceId());
    try {
      Long fromAccount = externalTask.getVariable(FROM_ACCOUNT);
      Long toAccount   = externalTask.getVariable(TO_ACCOUNT);
      try {
        accountService.getAccountById(fromAccount);
        accountService.getAccountById(toAccount);
      } catch (DataNotFoundException e) {
        log.warn("Account not found: {}", e.getMessage());
        externalTaskService.handleBpmnError(externalTask, "ACCOUNT_NOT_FOUND", e.getMessage(),null);
        return;
      }
      externalTaskService.complete(externalTask);
      log.info("Completed task: id={}", externalTask.getId());
    } catch (Exception e) {
      Long transferId = externalTask.getVariable(TRANSFER_ID);
      transferService.markAsFailed(transferId);
      log.error("CheckAccount task failed with technical error", e);
      int retries = externalTask.getRetries() == null ? 3 : externalTask.getRetries() - 1;
      externalTaskService.handleFailure(
        externalTask,
        e.getMessage(),
        e.getClass().getSimpleName(),
        retries,
        60_000L
      );
    }
  }
}