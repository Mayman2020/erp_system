package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.dto.display.BalanceSheetLineDto;
import com.erp.system.accounting.dto.display.BalanceSheetReportDto;
import com.erp.system.accounting.dto.display.ProfitLossLineDto;
import com.erp.system.accounting.dto.display.ProfitLossReportDto;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.JournalEntryLineRepository;
import com.erp.system.common.enums.AccountingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountingReportServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JournalEntryLineRepository journalEntryLineRepository;

    @Mock
    private CurrencyConversionService currencyConversionService;

    @InjectMocks
    private AccountingReportService accountingReportService;

    @Test
    void getProfitLossUsesDbAggregation() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);

        when(journalEntryLineRepository.aggregateRevenues(from, to)).thenReturn(List.of(
                new ProfitLossLineDto(2L, "4200", "Service Revenue", "إيرادات الخدمة", new BigDecimal("500.00"))
        ));
        when(journalEntryLineRepository.aggregateExpenses(from, to)).thenReturn(List.of(
                new ProfitLossLineDto(3L, "5100", "Salaries", "الرواتب", new BigDecimal("300.00"))
        ));

        ProfitLossReportDto report = accountingReportService.getProfitLoss(from, to);

        assertThat(report.getTotalRevenue()).isEqualByComparingTo("500.00");
        assertThat(report.getTotalExpenses()).isEqualByComparingTo("300.00");
        assertThat(report.getNetProfit()).isEqualByComparingTo("200.00");
        assertThat(report.getRevenues()).hasSize(1);
        assertThat(report.getExpenses()).hasSize(1);
    }

    @Test
    void getProfitLossReversalNetsToZero() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);

        when(journalEntryLineRepository.aggregateRevenues(from, to)).thenReturn(List.of());
        when(journalEntryLineRepository.aggregateExpenses(from, to)).thenReturn(List.of());

        ProfitLossReportDto report = accountingReportService.getProfitLoss(from, to);

        assertThat(report.getTotalRevenue()).isEqualByComparingTo("0.00");
        assertThat(report.getTotalExpenses()).isEqualByComparingTo("0.00");
        assertThat(report.getNetProfit()).isEqualByComparingTo("0.00");
    }

    @Test
    void getBalanceSheetIncludesOpeningBalancesAndCurrentEarnings() {
        LocalDate asOfDate = LocalDate.of(2026, 1, 31);

        Account cash = account(1L, "1110", "Cash", AccountingType.ASSET, bd("1000.00"), Account.BalanceSide.DEBIT);
        Account payable = account(2L, "2100", "Accounts Payable", AccountingType.LIABILITY, bd("500.00"), Account.BalanceSide.CREDIT);
        Account capital = account(3L, "3100", "Owner Capital", AccountingType.EQUITY, bd("500.00"), Account.BalanceSide.CREDIT);

        when(accountRepository.findAllByOrderByCodeAsc()).thenReturn(List.of(cash, payable, capital));

        when(journalEntryLineRepository.aggregateBalanceByType(eq(AccountingType.ASSET), eq(asOfDate)))
                .thenReturn(List.of(new BalanceSheetLineDto(1L, "1110", "Cash", "نقد", new BigDecimal("200.00"))));
        when(journalEntryLineRepository.aggregateBalanceByType(eq(AccountingType.LIABILITY), eq(asOfDate)))
                .thenReturn(List.of());
        when(journalEntryLineRepository.aggregateBalanceByType(eq(AccountingType.EQUITY), eq(asOfDate)))
                .thenReturn(List.of());
        when(journalEntryLineRepository.sumNetIncomeUpTo(asOfDate)).thenReturn(new BigDecimal("200.00"));
        when(journalEntryLineRepository.sumNetExpenseUpTo(asOfDate)).thenReturn(BigDecimal.ZERO);

        BalanceSheetReportDto report = accountingReportService.getBalanceSheet(asOfDate);

        assertThat(report.getTotalAssets()).isEqualByComparingTo("1200.00");
        assertThat(report.getTotalLiabilities()).isEqualByComparingTo("500.00");
        assertThat(report.getTotalEquity()).isEqualByComparingTo("700.00");
        assertThat(report.getLiabilitiesAndEquity()).isEqualByComparingTo("1200.00");
        assertThat(report.isBalanced()).isTrue();
        assertThat(report.getEquity()).anySatisfy(line -> {
            assertThat(line.getAccountCode()).isEqualTo("3999");
            assertThat(line.getBalance()).isEqualByComparingTo("200.00");
        });
    }

    @Test
    void getBalanceSheetWithoutOpeningBalancesUsesOnlyTransactions() {
        LocalDate asOfDate = LocalDate.of(2026, 3, 31);

        when(accountRepository.findAllByOrderByCodeAsc()).thenReturn(List.of());
        when(journalEntryLineRepository.aggregateBalanceByType(eq(AccountingType.ASSET), eq(asOfDate)))
                .thenReturn(List.of(new BalanceSheetLineDto(1L, "1110", "Cash", "نقد", new BigDecimal("500.00"))));
        when(journalEntryLineRepository.aggregateBalanceByType(eq(AccountingType.LIABILITY), eq(asOfDate)))
                .thenReturn(List.of(new BalanceSheetLineDto(2L, "2100", "AP", "ذمم", new BigDecimal("-300.00"))));
        when(journalEntryLineRepository.aggregateBalanceByType(eq(AccountingType.EQUITY), eq(asOfDate)))
                .thenReturn(List.of());
        when(journalEntryLineRepository.sumNetIncomeUpTo(asOfDate)).thenReturn(new BigDecimal("200.00"));
        when(journalEntryLineRepository.sumNetExpenseUpTo(asOfDate)).thenReturn(BigDecimal.ZERO);

        BalanceSheetReportDto report = accountingReportService.getBalanceSheet(asOfDate);

        assertThat(report.getTotalAssets()).isEqualByComparingTo("500.00");
        assertThat(report.getTotalLiabilities()).isEqualByComparingTo("300.00");
        assertThat(report.getTotalEquity()).isEqualByComparingTo("200.00");
        assertThat(report.isBalanced()).isTrue();
    }

    private Account account(Long id, String code, String name,
                            AccountingType accountType, BigDecimal openingBalance,
                            Account.BalanceSide openingBalanceSide) {
        Account account = new Account();
        account.setId(id);
        account.setCode(code);
        account.setNameEn(name);
        account.setNameAr(name);
        account.setAccountType(accountType);
        account.setOpeningBalance(openingBalance);
        account.setOpeningBalanceSide(openingBalanceSide);
        return account;
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
