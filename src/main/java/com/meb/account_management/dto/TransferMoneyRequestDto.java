package com.meb.account_management.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class TransferMoneyRequestDto {
    BigDecimal amount;
    Long sourceAccountId;
    Long targetAccountId;
}
