package com.meb.account_management.controller;

import com.meb.account_management.dto.ServiceResponse;
import com.meb.account_management.dto.TransferMoneyRequestDto;
import com.meb.account_management.dto.TransferMoneyResponseDto;
import com.meb.account_management.model.Account;
import com.meb.account_management.service.AccountService;
import lombok.val;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/account/")
public class AccountController {

    private final AccountService accountService;

    AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("open")
    public ServiceResponse<Account.AccountDto> openAccount(){
        val username = SecurityContextHolder.getContext().getAuthentication().getName();
        return accountService.createNewAccount(username);
    }

    @GetMapping("")
    public ServiceResponse<List<Account.AccountDto>> getMyAccountsInformation(){
        val username = SecurityContextHolder.getContext().getAuthentication().getName();
        return accountService.getAccountsByUser(username);
    }

    @PostMapping("transfer")
    public ServiceResponse<TransferMoneyResponseDto>  transferMoney(@RequestBody TransferMoneyRequestDto transferMoneyRequestDto){
        val username = SecurityContextHolder.getContext().getAuthentication().getName();
        return accountService.transferMoney(transferMoneyRequestDto,username);
    }

}
