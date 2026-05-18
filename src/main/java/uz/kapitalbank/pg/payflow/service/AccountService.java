package uz.kapitalbank.pg.payflow.service;

import uz.kapitalbank.pg.payflow.dto.request.AccountCreateRequest;
import uz.kapitalbank.pg.payflow.dto.response.AccountResponse;
import uz.kapitalbank.pg.payflow.entity.AccountEntity;

public interface AccountService {
    AccountResponse createAccount(AccountCreateRequest createRequest);

    boolean balanceChecker(Long fromAccountId, Long amount);

    boolean checkCurrencyOfTwoAccounts(Long fromAccountId, Long toAccountId, Long transferId);

    AccountEntity getAccountById(Long id);

    void debitAccount(Long fromAccount, Long amount);

    void creditAccount(Long toAccount, Long amount);
    void setDailyLimit(Long fromAccount, Long amount);

    void rollBackAccount(Long fromAccount, Long amount);
}
