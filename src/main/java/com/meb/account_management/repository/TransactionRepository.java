package com.meb.account_management.repository;

import com.meb.account_management.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

}
