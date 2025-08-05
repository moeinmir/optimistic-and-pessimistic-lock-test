package com.meb.account_management;

import com.meb.account_management.constant.TransactionType;
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
import org.junit.jupiter.api.Test;
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
	void testCreateAccount() {

		CustomUser userWithMoney = CustomUser.createNewUser("userwithmoney","userwithmoney","userwithmoney","password");
		customUserRepository.save(userWithMoney);
		Account firstAccountWithMoney = Account.createNewAccountWithBalanceForTheTest(userWithMoney);
		accountRepository.save(firstAccountWithMoney);
		Account secondAccountWithMoney = Account.createNewAccountWithBalanceForTheTest(userWithMoney);
		accountRepository.save(secondAccountWithMoney);
		CustomUser userWithoutMoney = CustomUser.createNewUser("userwithoutmoney","userwithoutmoney","userwithoutmoney","password");
		customUserRepository.save(userWithoutMoney);
		Account firstAccountWithoutMoney = Account.createNewAccount(userWithoutMoney);
		accountRepository.save(firstAccountWithoutMoney);
		Account secondAccountWithoutMoney = Account.createNewAccount(userWithoutMoney);
		accountRepository.save(secondAccountWithoutMoney);

		Long userWithMoneyId = userWithMoney.getUserInformation().getId();
		BigDecimal firstAccountWithMoneyBalanceBefore = firstAccountWithMoney.getAccountInformation().getBalance();
		Transaction pureDebitOnFirstAccountWithMoney = firstAccountWithMoney.addTransaction(BigDecimal.ONE, TransactionType.DEBIT, userWithMoneyId);
		transactionRepository.save(pureDebitOnFirstAccountWithMoney);
		accountRepository.save(firstAccountWithoutMoney);
		BigDecimal firstAccountWithMoneyBalanceAfter = firstAccountWithMoney.getAccountInformation().getBalance();
		assertEquals(firstAccountWithMoneyBalanceAfter, firstAccountWithMoneyBalanceBefore.subtract(BigDecimal.ONE));

	}
}
