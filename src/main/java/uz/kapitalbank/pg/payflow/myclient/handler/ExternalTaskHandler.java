package uz.kapitalbank.pg.payflow.myclient.handler;

import uz.kapitalbank.pg.payflow.myclient.builder.ExternalTaskBuilder;
import uz.kapitalbank.pg.payflow.myclient.service.ExternalTaskService;

@FunctionalInterface
public interface ExternalTaskHandler {
    void execute(ExternalTaskBuilder task, ExternalTaskService service) throws Exception;
  }

