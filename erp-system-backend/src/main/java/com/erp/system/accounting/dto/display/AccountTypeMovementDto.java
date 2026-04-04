package com.erp.system.accounting.dto.display;

import com.erp.system.common.enums.AccountingType;

import java.math.BigDecimal;

public record AccountTypeMovementDto(
        AccountingType accountType,
        BigDecimal debitTotal,
        BigDecimal creditTotal
) {
}
