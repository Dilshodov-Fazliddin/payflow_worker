package uz.kapitalbank.pg.payflow.service;


import uz.kapitalbank.pg.payflow.dto.request.TransferCreateRequest;
import uz.kapitalbank.pg.payflow.dto.response.TransferResponse;
import uz.kapitalbank.pg.payflow.entity.TransferEntity;

public interface TransferService {
  TransferResponse transferCreate(TransferCreateRequest transferCreateRequest);

  void markAsFailed(Long transferId);

  void debitAndCredit(Long transferId,String processInstanceId);

  Boolean checkAccountLimit(Long fromAccount, Long amount);

  TransferEntity getTransferById(Long id);
}
