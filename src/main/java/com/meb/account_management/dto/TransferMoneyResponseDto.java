package com.meb.account_management.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class TransferMoneyResponseDto {
    public Long sourceAccountId;
    public Long targetAccountId;
    public Long sourceAccountOwnerId;
    public Long targetAccountOwnerId;
    public BigDecimal amount;
    public BigDecimal sourceAccountRemainingBalance;
}
