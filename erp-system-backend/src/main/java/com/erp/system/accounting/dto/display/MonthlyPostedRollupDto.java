package com.erp.system.accounting.dto.display;

import java.math.BigDecimal;

public record MonthlyPostedRollupDto(
        int year,
        int month,
        BigDecimal debitTotal,
        BigDecimal creditTotal,
        BigDecimal revenueTotal,
        BigDecimal expenseTotal
) {}
