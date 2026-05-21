package uz.kapitalbank.pg.payflow.service.impl;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.kapitalbank.pg.payflow.constant.enums.TransferStatus;
import uz.kapitalbank.pg.payflow.dto.request.TransferCreateRequest;
import uz.kapitalbank.pg.payflow.dto.response.TransferResponse;
import uz.kapitalbank.pg.payflow.entity.AccountEntity;
import uz.kapitalbank.pg.payflow.entity.TransferEntity;
import uz.kapitalbank.pg.payflow.entity.UserEntity;
import uz.kapitalbank.pg.payflow.exception.DataNotFoundException;
import uz.kapitalbank.pg.payflow.exception.TransferCanceledException;
import uz.kapitalbank.pg.payflow.mapper.TransferMapper;
import uz.kapitalbank.pg.payflow.repository.TransferRepository;
import uz.kapitalbank.pg.payflow.service.AccountService;
import uz.kapitalbank.pg.payflow.service.CamundaStartTransferProcess;
import uz.kapitalbank.pg.payflow.service.TransferService;
import uz.kapitalbank.pg.payflow.service.UserService;

import java.time.LocalDateTime;

import static uz.kapitalbank.pg.payflow.constant.enums.TransferStatus.COMPENSATED;
import static uz.kapitalbank.pg.payflow.constant.enums.TransferStatus.FAILED;
import static uz.kapitalbank.pg.payflow.constant.enums.TransferStatus.WAITING;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransferServiceImpl implements TransferService {

  TransferRepository transferRepository;
  AccountService accountService;
  UserService userService;
  TransferMapper transferMapper;
  CamundaStartTransferProcess camundaStartTransferProcess;

  @Override
  public TransferResponse transferCreate(TransferCreateRequest transferCreateRequest) {
    TransferEntity transfer = transferMapper.toEntity(transferCreateRequest);
    transfer = transferRepository.save(transfer);

    UserEntity fromUser = userService.findByUserId(transferCreateRequest.getFromUserId());
    UserEntity toUser = userService.findByUserId(transferCreateRequest.getToUserId());

    transfer.setFromAccount(fromUser);
    transfer.setToAccount(toUser);

    transferRepository.save(transfer);
    camundaStartTransferProcess.startTransfer(transferMapper.toProcess(transfer));
    TransferEntity transferToResponse = transferRepository.findById(transfer.getId()).orElseThrow(() -> new DataNotFoundException("Transfer not found"));
    return transferMapper.toResponse(transferToResponse);
  }

  @Override
  public void markAsFailed(Long transferId) {
    TransferEntity transfer = getTransferById(transferId);

    transfer.setCompletedAt(LocalDateTime.now());
    transfer.setTransferStatus(FAILED);
    transferRepository.save(transfer);
  }


  @Override
  public Boolean checkAccountLimit(Long fromAccount, Long amount) {
    AccountEntity accountEntity = accountService.getAccountById(fromAccount);

    long transferAmount = accountEntity.getDailyLimitUsed() + amount;
    if (!(transferAmount > accountEntity.getDailyLimitMax())) {
      return true;
    } else {
      throw new TransferCanceledException("Transfer limit not allowed");
    }
  }

  @Override
  public TransferEntity getTransferById(Long id) {
    return transferRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Transfer not found"));
  }

  @Override
  public void debitAccount(Long fromAccount, Long amount, Long transferId) {
    try {
      accountService.debitAccount(fromAccount, amount);
      accountService.setDailyLimit(fromAccount, amount);
      changeTransferStatus(transferId, WAITING);
    } catch (Exception e) {
      changeTransferStatus(transferId, TransferStatus.FAILED);
      throw new TransferCanceledException("Transfer not allowed");
    }
  }

  @Override
  public void changeTransferStatus(Long transferId, TransferStatus transferStatus) {
    TransferEntity transfer = getTransferById(transferId);
    transfer.setTransferStatus(transferStatus);
    transferRepository.save(transfer);
  }

  @Override
  @Transactional
  public void credit(Long toAccount, Long amount, Long transferId) {
    try {
      accountService.creditAccount(toAccount, amount);
      TransferEntity transferById = getTransferById(transferId);
      transferById.setCompletedAt(LocalDateTime.now());
    } catch (Exception e) {
      changeTransferStatus(transferId, TransferStatus.FAILED);
      throw new TransferCanceledException("Transfer not allowed");
    }
  }

  @Override
  @Transactional
  public void rollBack(Long transferId) {
    TransferEntity transfer = getTransferById(transferId);

    if (transfer.getTransferStatus() == COMPENSATED || transfer.getTransferStatus() == FAILED) {
      log.info("Already rolled back transferId {}", transferId);
      return;
    }
    try {
      accountService.rollBackAccount(transfer.getFromAccount().getId(), transfer.getAmount());
      changeTransferStatus(transferId, COMPENSATED);
    } catch (Exception e) {
      changeTransferStatus(transferId, TransferStatus.FAILED);
    }
  }
}