package uz.kapitalbank.pg.payflow.service;


import uz.kapitalbank.pg.payflow.constant.enums.TransferStatus;
import uz.kapitalbank.pg.payflow.dto.request.TransferCreateRequest;
import uz.kapitalbank.pg.payflow.dto.response.TransferResponse;
import uz.kapitalbank.pg.payflow.entity.TransferEntity;

public interface TransferService {
  TransferResponse transferCreate(TransferCreateRequest transferCreateRequest);

  void markAsFailed(Long transferId);

  Boolean checkAccountLimit(Long fromAccount, Long amount);

  TransferEntity getTransferById(Long id);

  void debitAccount(Long fromAccount, Long amount,Long transferId);

  void changeTransferStatus(Long transferId, TransferStatus transferStatus);

  void credit(Long toAccount, Long amount, Long transferId);

  void rollBack(Long transferId);
}
