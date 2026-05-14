package uz.kapitalbank.pg.payflow.camunda.workers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import uz.kapitalbank.pg.payflow.camunda.constant.CamundaConstants;
import uz.kapitalbank.pg.payflow.myclient.builder.ExternalTaskBuilder;
import uz.kapitalbank.pg.payflow.myclient.handler.ExternalTaskHandler;
import uz.kapitalbank.pg.payflow.myclient.service.ExternalTaskService;
import uz.kapitalbank.pg.payflow.myclient.subscription.ExternalTaskSubscription;
import uz.kapitalbank.pg.payflow.service.AccountService;
import uz.kapitalbank.pg.payflow.service.TransferService;

import static uz.kapitalbank.pg.payflow.camunda.constant.CamundaConstants.AMOUNT;
import static uz.kapitalbank.pg.payflow.camunda.constant.CamundaConstants.CHECK_AMOUNT_TOPIC;
import static uz.kapitalbank.pg.payflow.camunda.constant.CamundaConstants.FROM_ACCOUNT;
import static uz.kapitalbank.pg.payflow.camunda.constant.CamundaConstants.TO_ACCOUNT;
import static uz.kapitalbank.pg.payflow.camunda.constant.CamundaConstants.TRANSFER_ID;

@Component
@ExternalTaskSubscription(
  topicName = CHECK_AMOUNT_TOPIC,
  lockDuration = 30000,
  variableNames = {FROM_ACCOUNT,AMOUNT, TO_ACCOUNT, TRANSFER_ID}
)
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CheckAmountBalanceWorker implements ExternalTaskHandler {
  AccountService accountService;
  TransferService transferService;

  @Override
  public void execute(ExternalTaskBuilder externalTask, ExternalTaskService externalTaskService) {
    try {
      Long amount = externalTask.getVariable(AMOUNT);
      Long fromAccount = externalTask.getVariable(FROM_ACCOUNT);
      Long toAccount = externalTask.getVariable(TO_ACCOUNT);
      Long transferId = externalTask.getVariable(TRANSFER_ID);

      if (accountService.balanceChecker(fromAccount, amount) && accountService.checkCurrencyOfTwoAccounts(fromAccount,toAccount,transferId)) {
        externalTaskService.complete(externalTask);
      } else {
        externalTaskService.handleBpmnError(externalTask, "INSUFFICIENT_BALANCE","Amount isn't enough", null);
      }
    } catch (Exception e) {
      transferService.markAsFailed(Long.valueOf(CamundaConstants.TRANSFER_ID));
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
