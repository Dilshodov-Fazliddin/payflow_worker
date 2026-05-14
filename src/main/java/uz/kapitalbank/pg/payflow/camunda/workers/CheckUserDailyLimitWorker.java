package uz.kapitalbank.pg.payflow.camunda.workers;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import uz.kapitalbank.pg.payflow.camunda.constant.CamundaConstants;
import uz.kapitalbank.pg.payflow.exception.DataNotFoundException;
import uz.kapitalbank.pg.payflow.exception.TransferCanceledException;
import uz.kapitalbank.pg.payflow.myclient.builder.ExternalTaskBuilder;
import uz.kapitalbank.pg.payflow.myclient.handler.ExternalTaskHandler;
import uz.kapitalbank.pg.payflow.myclient.service.ExternalTaskService;
import uz.kapitalbank.pg.payflow.myclient.subscription.ExternalTaskSubscription;
import uz.kapitalbank.pg.payflow.service.TransferService;

import java.io.PrintWriter;
import java.io.StringWriter;

import static uz.kapitalbank.pg.payflow.camunda.constant.CamundaConstants.*;

@Component
@ExternalTaskSubscription(
  topicName = CHECK_DAILY_LIMIT_TOPIC,
  lockDuration = 60000,
  variableNames = {FROM_ACCOUNT, AMOUNT}
)
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CheckUserDailyLimitWorker implements ExternalTaskHandler {

  TransferService transferService;

  @Override
  public void execute(ExternalTaskBuilder task, ExternalTaskService taskService) {
    MDC.put("processInstanceId", task.getProcessInstanceId());
    log.info("DailyLimit task received: id={}", task.getId());

    try {
      Long amount = task.getVariable(AMOUNT);
      Long fromAccount = task.getVariable(FROM_ACCOUNT);

      try {
        transferService.checkAccountLimit(fromAccount, amount);
      } catch (TransferCanceledException ex) {
        log.warn("Daily limit exceeded: account={}, amount={}", fromAccount, amount);
        taskService.handleBpmnError(task, "DAILY_LIMIT_EXCEEDED", ex.getMessage(),null);
        return;
      }
      taskService.complete(task);
      log.info("DailyLimit check passed");

    } catch (DataNotFoundException nfe) {
      log.warn("Task {} no longer exists, skipping", task.getId());
    } catch (Exception ex) {
      transferService.markAsFailed(Long.valueOf(CamundaConstants.TRANSFER_ID));
      log.error("DailyLimit failed with technical error", ex);
      int retries = task.getRetries() == null ? 3 : task.getRetries() - 1;
      try {
        taskService.handleFailure(
          task,
          ex.getMessage(),
          stackTraceToString(ex),
          retries,
          60_000L
        );
      } catch (DataNotFoundException nfe) {
        log.warn("Could not report failure — task gone");
      }
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