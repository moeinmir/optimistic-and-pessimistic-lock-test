package com.meb.account_management.controller;

import com.meb.account_management.dto.AddTransactionRequestDto;
import com.meb.account_management.dto.AddTransactionResponseDto;
import com.meb.account_management.dto.ServiceResponse;
import com.meb.account_management.model.Transaction;
import com.meb.account_management.service.TransactionService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transaction/")
public class TransactionController {

    private final TransactionService transactionService;

    TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    @PostMapping("add")
    public ServiceResponse<AddTransactionResponseDto> addTransaction(@RequestBody AddTransactionRequestDto addTransactionRequestDto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return transactionService.addTransactionByAdmin(addTransactionRequestDto,username);
    }
}
