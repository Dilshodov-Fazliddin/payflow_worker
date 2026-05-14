package uz.kapitalbank.pg.payflow.service.impl;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.kapitalbank.pg.payflow.dto.request.TransferCreateRequest;
import uz.kapitalbank.pg.payflow.dto.response.TransferResponse;
import uz.kapitalbank.pg.payflow.entity.AccountEntity;
import uz.kapitalbank.pg.payflow.entity.TransferEntity;
import uz.kapitalbank.pg.payflow.entity.UserEntity;
import uz.kapitalbank.pg.payflow.exception.DataNotFoundException;
import uz.kapitalbank.pg.payflow.exception.TransferCanceledException;
import uz.kapitalbank.pg.payflow.mapper.TransferMapper;
import uz.kapitalbank.pg.payflow.repository.AccountRepository;
import uz.kapitalbank.pg.payflow.repository.TransferRepository;
import uz.kapitalbank.pg.payflow.repository.UserRepository;
import uz.kapitalbank.pg.payflow.service.CamundaStartTransferProcess;
import uz.kapitalbank.pg.payflow.service.TransferService;

import java.time.LocalDateTime;

import static uz.kapitalbank.pg.payflow.constant.enums.TransferStatus.COMPLETED;
import static uz.kapitalbank.pg.payflow.constant.enums.TransferStatus.FAILED;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransferServiceImpl implements TransferService {

  TransferRepository transferRepository;
  AccountRepository accountRepository;
  UserRepository userRepository;
  TransferMapper transferMapper;
  CamundaStartTransferProcess camundaStartTransferProcess;

  @Override
  public TransferResponse transferCreate(TransferCreateRequest transferCreateRequest) {


    TransferEntity transfer = transferMapper.toEntity(transferCreateRequest);
    transfer = transferRepository.save(transfer);

    UserEntity fromUser = userRepository.findById(transferCreateRequest.getFromUserId()).orElseThrow(() -> new DataNotFoundException("From User not found"));
    UserEntity toUser = userRepository.findById(transferCreateRequest.getToUserId()).orElseThrow(() -> new DataNotFoundException("To User not found"));

    transfer.setFromAccount(fromUser);
    transfer.setToAccount(toUser);

    transferRepository.save(transfer);
    camundaStartTransferProcess.startTransfer(transferMapper.toProcess(transfer));
    TransferEntity transferToResponse = transferRepository.findById(transfer.getId()).orElseThrow(() -> new DataNotFoundException("Transfer not found"));
    return transferMapper.toResponse(transferToResponse);
  }

  @Override
  public void markAsFailed(Long transferId) {
    TransferEntity transfer = transferRepository.findById(transferId)
      .orElseThrow(() -> new DataNotFoundException("Transfer not found"));


    transfer.setCompletedAt(LocalDateTime.now());
    transfer.setTransferStatus(FAILED);
    transferRepository.save(transfer);
  }

  @Override
  @Transactional
  public void debitAndCredit(Long transferId,String processInstanceId) {

    TransferEntity transfer = transferRepository
      .findById(transferId)
      .orElseThrow(()-> new DataNotFoundException("Transfer not found"));

    AccountEntity fromAccount = accountRepository
      .findById(transfer.getFromAccount().getId())
      .orElseThrow(() -> new DataNotFoundException("Account not found wit id: " + transfer.getFromAccount().getId()));

    AccountEntity toAccount = accountRepository
      .findById(transfer.getToAccount().getId())
      .orElseThrow(() -> new DataNotFoundException("Account not found wit id: " + transfer.getToAccount().getId()));


    log.info("Transfer from {} to {} started", fromAccount.getId(), toAccount.getId());

    Long amount = transfer.getAmount();

    fromAccount.setBalance(fromAccount.getBalance() - amount);
    toAccount.setBalance(toAccount.getBalance() + amount);

    fromAccount.setDailyLimitUsed(fromAccount.getDailyLimitUsed() + amount);
    transfer.setProcessInstanceId(processInstanceId);
    transfer.setTransferStatus(COMPLETED);
    transfer.setCompletedAt(LocalDateTime.now());
  }


  @Override
  public Boolean checkAccountLimit(Long fromAccount, Long amount) {
    AccountEntity accountEntity = accountRepository.findById(fromAccount).orElseThrow(() -> new DataNotFoundException("From Account not found"));

    long transferAmount = accountEntity.getDailyLimitUsed() + amount;
    if (!(transferAmount > accountEntity.getDailyLimitMax())){
      return true;
    }else{
      throw new TransferCanceledException("Transfer limit not allowed");
    }
  }

  @Override
  public TransferEntity getTransferById(Long id) {
    return transferRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Transfer not found"));
  }
}
