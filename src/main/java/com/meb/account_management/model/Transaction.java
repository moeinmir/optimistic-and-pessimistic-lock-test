package com.meb.account_management.model;

import com.meb.account_management.constant.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;


@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Getter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    TransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    Account account;
    BigDecimal amount;

}
