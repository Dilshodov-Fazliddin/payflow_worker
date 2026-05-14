package uz.kapitalbank.pg.payflow.service.impl;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import uz.kapitalbank.pg.payflow.constant.enums.AccountStatus;
import uz.kapitalbank.pg.payflow.dto.request.AccountCreateRequest;
import uz.kapitalbank.pg.payflow.dto.response.AccountResponse;
import uz.kapitalbank.pg.payflow.entity.AccountEntity;
import uz.kapitalbank.pg.payflow.entity.TransferEntity;
import uz.kapitalbank.pg.payflow.entity.UserEntity;
import uz.kapitalbank.pg.payflow.exception.DataNotFoundException;
import uz.kapitalbank.pg.payflow.exception.TransferCanceledException;
import uz.kapitalbank.pg.payflow.mapper.AccountMapper;
import uz.kapitalbank.pg.payflow.repository.AccountRepository;
import uz.kapitalbank.pg.payflow.service.AccountService;
import uz.kapitalbank.pg.payflow.service.TransferService;
import uz.kapitalbank.pg.payflow.service.UserService;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class AccountServiceImpl implements AccountService {

    AccountRepository accountRepository;
    AccountMapper accountMapper;
    UserService userService;
    TransferService transferService;

    @Override
    public AccountResponse createAccount(AccountCreateRequest createRequest) {
        UserEntity user = userService.findByUserId(createRequest.getUserId());
        AccountEntity accountEntity = accountMapper.toEntity(createRequest);
        accountEntity.setAccountStatus(AccountStatus.ACTIVE);
        accountEntity.setUser(user);
        return accountMapper.toResponse(accountRepository.save(accountEntity));
    }

    @Override
    public boolean balanceChecker(Long fromAccountId, Long amount)  {
        AccountEntity accountEntity = accountRepository.findById(fromAccountId).orElseThrow();

        if (accountEntity.getBalance() == null || amount == null) {
            return false;
        }

      return accountEntity.getAccountStatus().equals(AccountStatus.ACTIVE) && accountEntity.getBalance() >= amount;
    }

    @Override
    public boolean checkCurrencyOfTwoAccounts(Long fromAccountId, Long toAccountId, Long transferId) {
        TransferEntity transfer = transferService.getTransferById(transferId);
        AccountEntity from = accountRepository.findById(fromAccountId).orElseThrow();
        AccountEntity to = accountRepository.findById(toAccountId).orElseThrow();
        if ((from.getCurrency().equals(to.getCurrency())) && from.getCurrency().equals(transfer.getCurrency())) {
            return true;
        }else {
            throw new TransferCanceledException("Currencies are not same");
        }

    }

    @Override
    public AccountEntity getAccountById(Long id) {
        return accountRepository.findById(id).orElseThrow(()->new DataNotFoundException("Account not found"));
    }




}
