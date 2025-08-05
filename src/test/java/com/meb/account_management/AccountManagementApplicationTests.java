package com.meb.account_management;

import com.meb.account_management.constant.TransactionType;
import com.meb.account_management.dto.ServiceResponse;
import com.meb.account_management.dto.TransferMoneyRequestDto;
import com.meb.account_management.dto.TransferMoneyResponseDto;
import com.meb.account_management.model.Account;
import com.meb.account_management.model.CustomUser;
import com.meb.account_management.model.Transaction;
import com.meb.account_management.repository.AccountRepository;
import com.meb.account_management.repository.CustomUserRepository;
import com.meb.account_management.repository.TransactionRepository;
import com.meb.account_management.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Profile("test")
@SpringBootTest
class AccountManagementApplicationTests {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomUserRepository customUserRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionRepository transactionRepository;


    @Test
    void contextLoads() {
    }

    @Test
    void testCreateAccount() throws InterruptedException {
        CustomUser userWithMoney = CustomUser.createNewUser("userwithmoney", "userwithmoney", "userwithmoney", "password");
        customUserRepository.save(userWithMoney);
        Account accountWithMoney = Account.createNewAccountWithBalanceForTheTest(userWithMoney);
        accountRepository.save(accountWithMoney);
        CustomUser userWithoutMoney = CustomUser.createNewUser("userwithoutmoney", "userwithoutmoney", "userwithoutmoney", "password");
        customUserRepository.save(userWithoutMoney);
        Account accountWithoutMoney = Account.createNewAccount(userWithoutMoney);
        accountRepository.save(accountWithoutMoney);
        Account secondAccountWithoutMoney = Account.createNewAccount(userWithoutMoney);
        accountRepository.save(secondAccountWithoutMoney);
        Long userWithMoneyId = userWithMoney.getUserInformation().getId();
        BigDecimal firstAccountWithMoneyBalanceBefore = accountWithMoney.getAccountInformation().getBalance();
        Transaction pureDebitOnFirstAccountWithMoney = accountWithMoney.addTransaction(BigDecimal.ONE, TransactionType.DEBIT, userWithMoneyId);
        transactionRepository.save(pureDebitOnFirstAccountWithMoney);
        accountRepository.save(accountWithoutMoney);
        BigDecimal firstAccountWithMoneyBalanceAfter = accountWithMoney.getAccountInformation().getBalance();
        assertEquals(firstAccountWithMoneyBalanceAfter, firstAccountWithMoneyBalanceBefore.subtract(BigDecimal.ONE));
    }

    @Test
    public void testTransferMoney(){

        CustomUser userWithMoney = CustomUser.createNewUser("userwithmoney", "userwithmoney", "userwithmoney", "password");
        customUserRepository.save(userWithMoney);
        Account accountWithMoney = Account.createNewAccountWithBalanceForTheTest(userWithMoney);
        accountRepository.save(accountWithMoney);
        CustomUser userWithoutMoney = CustomUser.createNewUser("userwithoutmoney", "userwithoutmoney", "userwithoutmoney", "password");
        customUserRepository.save(userWithoutMoney);
        Account accountWithoutMoney = Account.createNewAccount(userWithoutMoney);
        accountRepository.save(accountWithoutMoney);
        Account secondAccountWithoutMoney = Account.createNewAccount(userWithoutMoney);
        accountRepository.save(secondAccountWithoutMoney);
        TransferMoneyRequestDto transferFromFirstRequestDto = TransferMoneyRequestDto.builder()
                .sourceAccountId(accountWithMoney.getAccountInformation().getId())
                .targetAccountId(accountWithoutMoney.getAccountInformation().getId())
                .amount(BigDecimal.ONE).build();
        String firstRequesterUsername = userWithMoney.getUserInformation().getUsername();
        ServiceResponse<TransferMoneyResponseDto> transferMoneyResponse = accountService.transferMoney(transferFromFirstRequestDto, firstRequesterUsername);

        if (transferMoneyResponse.isSuccess()){
            System.out.println(transferMoneyResponse.getResult().getSourceAccountRemainingBalance());
        }
    }

    @Test
    public void testOptimisticLock() throws InterruptedException {
        CustomUser userWithMoney = CustomUser.createNewUser("userwithmoney", "userwithmoney", "userwithmoney", "password");
        customUserRepository.save(userWithMoney);
        Account firstAccountWithMoney = Account.createNewAccountWithBalanceForTheTest(userWithMoney);
        accountRepository.save(firstAccountWithMoney);
        Account secondAccountWithMoney = Account.createNewAccountWithBalanceForTheTest(userWithMoney);
        accountRepository.save(secondAccountWithMoney);
        CustomUser userWithoutMoney = CustomUser.createNewUser("userwithoutmoney", "userwithoutmoney", "userwithoutmoney", "password");
        customUserRepository.save(userWithoutMoney);
        Account firstAccountWithoutMoney = Account.createNewAccount(userWithoutMoney);
        accountRepository.save(firstAccountWithoutMoney);
        Account secondAccountWithoutMoney = Account.createNewAccount(userWithoutMoney);
        accountRepository.save(secondAccountWithoutMoney);
        int NUMBER_OF_TRIALS_OF_FIRST_ACCOUNT_WITH_MONEY = 500;
        int NUMBER_OF_TRIALS_OF_SECOND_ACCOUNT_WITH_MONEY = 500;
        AtomicInteger numberOfSuccessFullTransfersFromFirstAccountWithMoney = new AtomicInteger();
        AtomicInteger numberOfSuccessFullTransfersFromSecondAccountWithMoney = new AtomicInteger();


        Runnable transferFromFirst = () -> {
            TransferMoneyRequestDto transferFromFirstRequestDto = TransferMoneyRequestDto.builder()
                    .sourceAccountId(firstAccountWithMoney.getAccountInformation().getId())
                    .targetAccountId(firstAccountWithoutMoney.getAccountInformation().getId())
                    .amount(BigDecimal.ONE).build();
            String firstRequesterUsername = userWithMoney.getUserInformation().getUsername();
            for (int i = 0; i < NUMBER_OF_TRIALS_OF_FIRST_ACCOUNT_WITH_MONEY; i++) {
                ServiceResponse<TransferMoneyResponseDto> transferMoneyResponse = accountService.transferMoney(transferFromFirstRequestDto, firstRequesterUsername);
            	if (transferMoneyResponse.isSuccess()){
                    numberOfSuccessFullTransfersFromFirstAccountWithMoney.addAndGet(1);
                }
            }
        };

        Runnable transferFromSecond = () -> {
            String secondRequesterUsername = userWithMoney.getUserInformation().getUsername();
            TransferMoneyRequestDto transferFromSecondRequestDto = TransferMoneyRequestDto.builder()
                    .sourceAccountId(secondAccountWithMoney.getAccountInformation().getId())
                    .targetAccountId(firstAccountWithoutMoney.getAccountInformation().getId())
                    .amount(BigDecimal.ONE).build();
            for (int i = 0; i < NUMBER_OF_TRIALS_OF_SECOND_ACCOUNT_WITH_MONEY; i++) {
                ServiceResponse<TransferMoneyResponseDto> transferMoneyResponse = accountService.transferMoney(transferFromSecondRequestDto, secondRequesterUsername);
                if(transferMoneyResponse.isSuccess()){
                    numberOfSuccessFullTransfersFromSecondAccountWithMoney.addAndGet(1);

                }
            }
        };

        Thread thread1 = new Thread(transferFromFirst);
        Thread thread2 = new Thread(transferFromSecond);
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        System.out.println(numberOfSuccessFullTransfersFromFirstAccountWithMoney);
        System.out.println(numberOfSuccessFullTransfersFromSecondAccountWithMoney);
    }
}


