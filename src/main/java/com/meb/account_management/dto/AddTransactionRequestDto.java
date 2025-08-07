package com.meb.account_management.dto;

import com.meb.account_management.constant.TransactionType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class AddTransactionRequestDto {
    public TransactionType type;
    public Long accountId;
    public BigDecimal amount;
}
