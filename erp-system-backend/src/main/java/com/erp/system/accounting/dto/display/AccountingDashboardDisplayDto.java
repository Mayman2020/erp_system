package com.erp.system.accounting.dto.display;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountingDashboardDisplayDto {

    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal totalEquity;
    private BigDecimal totalRevenue;
    private BigDecimal totalExpenses;
    private BigDecimal netProfit;
    private BigDecimal monthDebitTotal;
    private BigDecimal monthCreditTotal;
    /** Posted journal line debits per calendar day for the last 7 days (oldest → newest). */
    private List<BigDecimal> weekDebitSeries;
    /** Posted journal line credits per calendar day for the last 7 days (oldest → newest). */
    private List<BigDecimal> weekCreditSeries;
    /** Total debits per month for the rolling 12 months ending in the current month (oldest → newest). */
    private List<BigDecimal> rollingMonthDebitSeries;
    /** Total credits per month for the rolling 12 months ending in the current month (oldest → newest). */
    private List<BigDecimal> rollingMonthCreditSeries;
    private List<RecentDocument> recentJournals;
    private List<RecentDocument> recentPayments;
    private List<RecentDocument> recentReceipts;
    private BigDecimal receivablesSummary;
    private BigDecimal payablesOutstanding;
    private BigDecimal payablesPaid;
    private List<BudgetSnapshot> budgetSummaries;
    private List<BankBalance> bankBalances;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentDocument {
        private Long id;
        private String reference;
        private LocalDate date;
        private BigDecimal amount;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BudgetSnapshot {
        private Long id;
        private String label;
        private BigDecimal planned;
        private BigDecimal actual;
        private BigDecimal variance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BankBalance {
        private Long id;
        private String bankName;
        private String accountNumber;
        private BigDecimal balance;
        private String currency;
    }
}
