package com.meb.account_management.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
public class AddTransactionResponseDto {
    public Long transactionId;
    public Long accountId;
    public BigDecimal amount;
    public BigDecimal finalBalance;
}
