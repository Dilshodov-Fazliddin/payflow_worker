package uz.kapitalbank.pg.payflow.camunda.workers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import uz.kapitalbank.pg.payflow.exception.DataNotFoundException;
import uz.kapitalbank.pg.payflow.exception.TransferCanceledException;
import uz.kapitalbank.pg.payflow.myclient.builder.ExternalTaskBuilder;
import uz.kapitalbank.pg.payflow.myclient.handler.ExternalTaskHandler;
import uz.kapitalbank.pg.payflow.myclient.service.ExternalTaskService;
import uz.kapitalbank.pg.payflow.myclient.subscription.ExternalTaskSubscription;
import uz.kapitalbank.pg.payflow.service.TransferService;

import java.io.PrintWriter;
import java.io.StringWriter;

import static uz.kapitalbank.pg.payflow.camunda.constant.CamundaConstants.ROLLBACK_WORKER;
import static uz.kapitalbank.pg.payflow.camunda.constant.CamundaConstants.TRANSFER_ID;

@Slf4j
@Component
@RequiredArgsConstructor
@ExternalTaskSubscription(
  topicName = ROLLBACK_WORKER,
  lockDuration = 30_000L,
  variableNames = {TRANSFER_ID}
)
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class RollbackWorker implements ExternalTaskHandler {

  TransferService transferService;

  @Override
  public void execute(ExternalTaskBuilder task, ExternalTaskService service) throws Exception {
    MDC.put("processInstanceId", task.getProcessInstanceId());
    log.info("FraudCheck task received: id={}", task.getId());

    try {
      Long transfer = task.getVariable(TRANSFER_ID);
      try {
        transferService.rollBack(transfer);
      } catch (TransferCanceledException ex) {
        log.warn("We can't initialize your roll back transferId {}", transfer);
        service.handleBpmnError(task, "ROLLBACK_ERROR", ex.getMessage(), null);
        return;
      }
      service.complete(task);
      log.info("ROLLBACK task completed: id={}", task.getId());
    } catch (DataNotFoundException ex) {
      log.warn("Task {} no longer exists", task.getId());
    } catch (Exception ex) {
      transferService.markAsFailed(task.getVariable(TRANSFER_ID));
      int retries = task.getRetries() == null ? 3 : task.getRetries() - 1;
      try {
        service.handleFailure(
          task,
          ex.getMessage(),
          stackTraceToString(ex),
          retries,
          60_000L
        );
      }catch (DataNotFoundException ex2) {
        log.warn("Could not report failure — task gone {}", task.getId());
      }
    }finally {
      MDC.clear();
    }
  }
  private String stackTraceToString(Throwable t) {
    StringWriter sw = new StringWriter();
    t.printStackTrace(new PrintWriter(sw));
    return sw.toString();
  }
}
