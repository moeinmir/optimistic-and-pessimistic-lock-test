package com.meb.account_management.service;

import com.meb.account_management.dto.ServiceResponse;
import com.meb.account_management.dto.TransferMoneyRequestDto;
import com.meb.account_management.dto.TransferMoneyResponseDto;
import com.meb.account_management.model.Account;
import jakarta.transaction.Transactional;

import java.util.List;

public interface AccountService {

    ServiceResponse<Account.AccountDto> createNewAccount(String username);

    ServiceResponse<List<Account.AccountDto>> getAccountsByUser(String username);

    ServiceResponse<Account>  getAccountById(Long accountId);

    @Transactional
    ServiceResponse<TransferMoneyResponseDto> transferMoney(TransferMoneyRequestDto transferMoneyRequestDto, String username);
}
