package com.meb.account_management.model;

import com.meb.account_management.constant.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

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

    @Setter
    @Getter
    @Builder
    public static class TransactionDto {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        public Long id;

        @Enumerated(EnumType.STRING)
        TransactionType type;

        public BigDecimal amount;
    }

    public TransactionDto getTransactionInformation(){
        return TransactionDto.builder().id(this.id).amount(this.amount).type(this.type).build();
    }
}

