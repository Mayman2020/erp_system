package com.erp.system.accounting.dto.display;

import com.erp.system.common.enums.AccountingType;

import java.math.BigDecimal;

public record AccountTypeAmountDto(
        AccountingType accountType,
        BigDecimal amount
) {
}
