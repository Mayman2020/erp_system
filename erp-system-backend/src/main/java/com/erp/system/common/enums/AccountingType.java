package com.erp.system.common.enums;

public enum AccountingType {
    ASSET,
    LIABILITY,
    EQUITY,
    REVENUE,
    EXPENSE;

    public boolean isBalanceSheet() {
        return this == ASSET || this == LIABILITY || this == EQUITY;
    }

    public boolean isIncomeStatement() {
        return this == REVENUE || this == EXPENSE;
    }

    public String financialStatement() {
        return isBalanceSheet() ? "BALANCE_SHEET" : "INCOME_STATEMENT";
    }
}
