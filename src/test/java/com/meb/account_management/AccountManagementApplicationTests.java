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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
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
    public void testTransferMoney() {
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

        if (transferMoneyResponse.isSuccess()) {
            System.out.println(transferMoneyResponse.getResult().getSourceAccountRemainingBalance());
        }
    }

    @Test
    public void testOptimisticLockForTransfer() throws InterruptedException {
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
                if (transferMoneyResponse.isSuccess()) {
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
                if (transferMoneyResponse.isSuccess()) {
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

    @Test
    public void testOptimisticLockPreventingSaveWhenItShould() throws InterruptedException {
        CustomUser optUser = CustomUser.createNewUser("optuser", "optuser", "optuser", "password");
        customUserRepository.save(optUser);
        Account optAccount = Account.createNewAccountWithBalanceForTheTest(optUser);
        accountRepository.save(optAccount);
        Long optUserId = optAccount.getAccountInformation().getId();
        Long optAccountId = optAccount.getAccountInformation().getId();
        Object lock = new Object();
        AtomicBoolean transactionInFirstThreadDone = new AtomicBoolean(false);
        Thread firstThread = new Thread(() -> {
            Account optAccountInFirstThread = accountRepository.findById(optAccountId).orElse(null);
            synchronized (lock) {
                Transaction transactionInFirstThread = optAccountInFirstThread.addTransaction(BigDecimal.ONE, TransactionType.DEBIT, optUserId);
                transactionInFirstThreadDone.set(true);
                lock.notify();
                try {
                    lock.wait();  // Wait for secondThread to finish its work
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    transactionRepository.save(transactionInFirstThread);
                    accountRepository.save(optAccountInFirstThread);
                } catch (Exception e) {
                    System.out.println("the transaction in first thread could not be saved as we expected");
                    System.out.println(e.getMessage());
                }
            }
        });

        Thread secondThread = new Thread(() -> {
            Account optAccountInSecondThread = accountRepository.findById(optAccountId).orElse(null);
            synchronized (lock) {
                while (!transactionInFirstThreadDone.get()) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                Transaction transactionInSecondThread = optAccountInSecondThread.addTransaction(BigDecimal.ONE, TransactionType.DEBIT, optUserId);
                transactionRepository.save(transactionInSecondThread);
                accountRepository.save(optAccountInSecondThread);
                System.out.println("transaction in second thread has been saved as we expected");
                lock.notify();
            }
        });
        firstThread.start();
        secondThread.start();
        firstThread.join();
        secondThread.join();
    }


    @Test
    public void testOptimisticLockNotPreventingSaveWhenItShould() throws InterruptedException {
        CustomUser optUser = CustomUser.createNewUser("optuser", "optuser", "optuser", "password");
        customUserRepository.save(optUser);
        Account optAccount = Account.createNewAccountWithBalanceForTheTest(optUser);
        accountRepository.save(optAccount);
        Long optUserId = optAccount.getAccountInformation().getId();
        Long optAccountId = optAccount.getAccountInformation().getId();
        Object lock = new Object();
        AtomicBoolean transactionInFirstThreadDone = new AtomicBoolean(false);
        AtomicBoolean transactionInSecondThreadDone = new AtomicBoolean(false);
        Thread firstThread = new Thread(() -> {
            Account optAccountInFirstThread = accountRepository.findById(optAccountId).orElse(null);
            synchronized (lock) {
                Transaction transactionInFirstThread = optAccountInFirstThread.addTransaction(BigDecimal.ONE, TransactionType.DEBIT, optUserId);
                transactionRepository.save(transactionInFirstThread);
                accountRepository.save(optAccountInFirstThread);
                System.out.println("transaction in first thread has been saved as we expected");
                transactionInFirstThreadDone.set(true);
                lock.notify();
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Account optAccountAfterTransactionInSecondThread = accountRepository.findById(optAccountId).orElse(null);
            System.out.println("account balance after two transaction");
            System.out.println(optAccountAfterTransactionInSecondThread.getAccountInformation().getBalance());
        });
        Thread secondThread = new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Account optAccountInSecondThread = accountRepository.findById(optAccountId).orElse(null);
            synchronized (lock) {
                Transaction transactionInSecondThread = optAccountInSecondThread.addTransaction(BigDecimal.ONE, TransactionType.DEBIT, optUserId);
                transactionRepository.save(transactionInSecondThread);
                accountRepository.save(optAccountInSecondThread);
                System.out.println("transaction in second thread has been saved as we expected");
                transactionInSecondThreadDone.set(true);
                lock.notify();
            }
        });
        firstThread.start();
        secondThread.start();
        firstThread.join();
        secondThread.join();
    }

    @Test
    public void testPessimisticLockWithWithExecutor() throws InterruptedException {
        CustomUser optUser = CustomUser.createNewUser("optuser", "optuser", "optuser", "password");
        customUserRepository.save(optUser);
        Account optAccount = Account.createNewAccountWithBalanceForTheTest(optUser);
        accountRepository.save(optAccount);
        Long optUserId = optAccount.getAccountInformation().getId();
        Long optAccountId = optAccount.getAccountInformation().getId();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Runnable firstTask = () -> {
            System.out.println("account balance after two transaction");
            Account optAccountInFirstThread = accountRepository.findByIdWithPessimisticLock(optAccountId).orElse(null);
            Transaction transactionInFirstThread = optAccountInFirstThread.addTransaction(BigDecimal.ONE, TransactionType.DEBIT, optUserId);
            transactionRepository.save(transactionInFirstThread);
            accountRepository.save(optAccountInFirstThread);
        };
        Runnable secondTask = () -> {
            System.out.println("account balance after two transaction");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                Account optAccountInSecondThread = accountRepository.findByIdWithPessimisticLock(optAccountId).orElse(null);
                if (optAccountInSecondThread == null) {
                    throw new RuntimeException();
                }
            }
            catch (Exception e) {
                System.out.println("we could not access the account because first thread locked it");
                System.out.println(e.getMessage());
            }
        };
        executor.submit(firstTask);
        executor.submit(secondTask);
        executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
    }



}


