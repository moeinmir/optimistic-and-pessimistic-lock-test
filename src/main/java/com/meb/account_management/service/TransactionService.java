package com.meb.account_management.service;

import com.meb.account_management.dto.AddTransactionRequestDto;
import com.meb.account_management.dto.AddTransactionResponseDto;
import com.meb.account_management.dto.ServiceResponse;

public interface TransactionService {

    ServiceResponse<AddTransactionResponseDto> addTransactionByAdmin(AddTransactionRequestDto addTransactionRequestDto, String username);
}
