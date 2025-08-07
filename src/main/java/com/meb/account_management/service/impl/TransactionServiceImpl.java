package com.meb.account_management.service.impl;

import com.meb.account_management.dto.AddTransactionRequestDto;
import com.meb.account_management.dto.AddTransactionResponseDto;
import com.meb.account_management.dto.ServiceResponse;
import com.meb.account_management.model.Account;
import com.meb.account_management.model.CustomUser;
import com.meb.account_management.model.Transaction;
import com.meb.account_management.repository.TransactionRepository;
import com.meb.account_management.service.AccountService;
import com.meb.account_management.service.TransactionService;
import com.meb.account_management.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final UserService userService;
    TransactionServiceImpl(TransactionRepository transactionRepository,  AccountService accountService, UserService userService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.userService = userService;
    }

    public void getTransactionHistoryByAdmin(){}

    @Override
    public ServiceResponse<AddTransactionResponseDto> addTransactionByAdmin(AddTransactionRequestDto addTransactionRequestDto, String username){
        ServiceResponse<CustomUser> fetchedUser = userService.getUserByUserName(username);
        if (!fetchedUser.isSuccess()) {return ServiceResponse.failure();}
        ServiceResponse<Account> fetchedAccount = accountService.getAccountById(addTransactionRequestDto.getAccountId());
        if (!fetchedAccount.isSuccess()) {return ServiceResponse.failure();}
        Transaction transaction = fetchedAccount.getResult()
                .addTransaction(addTransactionRequestDto.amount,addTransactionRequestDto.type,fetchedUser.getResult().getUserInformation().getId(),fetchedUser.getResult().roles);
        transactionRepository.save(transaction);
        AddTransactionResponseDto responseDto = AddTransactionResponseDto.builder().transactionId(transaction.getId())
                .accountId(fetchedAccount.getResult().getAccountInformation().getId())
                .amount(transaction.getAmount())
                .finalBalance(fetchedAccount.getResult().getAccountInformation().getBalance())
                .build();
        return ServiceResponse.success(responseDto);
    }
}
