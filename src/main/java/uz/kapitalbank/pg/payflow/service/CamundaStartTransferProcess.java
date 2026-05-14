package uz.kapitalbank.pg.payflow.service;

import uz.kapitalbank.pg.payflow.dto.request.TransferToProcess;
import uz.kapitalbank.pg.payflow.dto.response.StartTransferResponse;

public interface CamundaStartTransferProcess {
  StartTransferResponse startTransfer(TransferToProcess request);
}
