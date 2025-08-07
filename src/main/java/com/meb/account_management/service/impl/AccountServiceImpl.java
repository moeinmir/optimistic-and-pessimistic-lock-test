package com.meb.account_management.service.impl;

import com.meb.account_management.constant.TransactionType;
import com.meb.account_management.dto.ServiceResponse;
import com.meb.account_management.dto.TransferMoneyRequestDto;
import com.meb.account_management.dto.TransferMoneyResponseDto;
import com.meb.account_management.model.Account;
import com.meb.account_management.model.Transaction;
import com.meb.account_management.repository.AccountRepository;
import com.meb.account_management.repository.TransactionRepository;
import com.meb.account_management.service.AccountService;
import com.meb.account_management.service.UserService;
import jakarta.transaction.Transactional;
import lombok.val;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private final UserService userService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    AccountServiceImpl(UserService userService, AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.userService = userService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public ServiceResponse<Account.AccountDto> createNewAccount(String username){
        val fetchedUser = userService.getUserByUserName(username);
        if (fetchedUser.isSuccess()){
            val newAccount = Account.createNewAccount(fetchedUser.getResult());
            accountRepository.save(newAccount);
            return ServiceResponse.success(newAccount.getAccountInformation());
        }
        return ServiceResponse.failure();
    }

    @Override
    public ServiceResponse<List<Account.AccountDto>> getAccountsByUser(String username){
        val fetchedUser = userService.getUserByUserName(username);
        if (fetchedUser.isSuccess()){
            val accounts = accountRepository.findByOwner(fetchedUser.getResult());
            val accountDtos = accounts.stream().map(Account::getAccountInformation).toList();
            return ServiceResponse.success(accountDtos);
        }
        return ServiceResponse.failure();
    }

    @Override
    public ServiceResponse<Account>  getAccountById(Long accountId){
        val optionalAccount = accountRepository.findById(accountId);
        return ServiceResponse.fromOptional(optionalAccount);
    }

    @Transactional
    @Override
    public ServiceResponse<TransferMoneyResponseDto> transferMoney(TransferMoneyRequestDto transferMoneyRequestDto, String username) {
        try {
            val fetchedUser = userService.getUserByUserName(username);
            if (!fetchedUser.isSuccess()) {
                throw new RuntimeException();
            }
            val fetchedSourceAccount = getAccountById(transferMoneyRequestDto.getSourceAccountId());
            if (!fetchedSourceAccount.isSuccess()) {
                throw new RuntimeException();
            }
            val fetchedTargetAccount = getAccountById(transferMoneyRequestDto.getTargetAccountId());
            if (!fetchedTargetAccount.isSuccess()) {
                throw new RuntimeException();
            }
            val sourceTransaction = fetchedSourceAccount.getResult().addTransaction(transferMoneyRequestDto.getAmount(), TransactionType.DEBIT, fetchedUser.getResult().getUserInformation().id,fetchedUser.getResult().roles);
            val targetTransaction = fetchedTargetAccount.getResult().addTransaction(transferMoneyRequestDto.getAmount(), TransactionType.CREDIT, fetchedUser.getResult().getUserInformation().id,fetchedUser.getResult().roles);
            transactionRepository.save(sourceTransaction);
            transactionRepository.save(targetTransaction);
            val responseDto = TransferMoneyResponseDto.builder()
                    .sourceAccountId(fetchedSourceAccount.getResult().getAccountInformation().id)
                    .targetAccountId(fetchedTargetAccount.getResult().getAccountInformation().id)
                    .amount(transferMoneyRequestDto.getAmount())
                    .targetAccountId(fetchedTargetAccount.getResult().getId())
                    .amount(transferMoneyRequestDto.getAmount())
                    .sourceAccountOwnerId(fetchedSourceAccount.getResult().getAccountInformation().getOwnerId())
                    .targetAccountOwnerId(fetchedTargetAccount.getResult().getAccountInformation().getOwnerId())
                    .sourceAccountRemainingBalance(fetchedSourceAccount.getResult().getAccountInformation().getBalance()).build();
            return ServiceResponse.success(responseDto);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            return ServiceResponse.failure();
        }
    }

    @Override
    public ServiceResponse<List<Transaction.TransactionDto>> getAccountTransactionHistoryByAccountId(Long accountId, String username){
        ServiceResponse<Account> fetchedAccount =  getAccountById(accountId);
        val fetchedUser = userService.getUserByUserName(username);
        if (!fetchedUser.isSuccess()) {
            throw new RuntimeException();
        }
        if (fetchedAccount.isSuccess()){
            val account = fetchedAccount.getResult();
            return ServiceResponse.success(account.getAccountTransactions(fetchedUser.getResult().getUserInformation().id));
        }
        return ServiceResponse.failure();
    }




    public void getAccountsByAdmin(){}
    public void addAccountByAdmin(){}
    public void editAccountByAdmin(){}
    public void deleteAccountByAdmin(){}

}

