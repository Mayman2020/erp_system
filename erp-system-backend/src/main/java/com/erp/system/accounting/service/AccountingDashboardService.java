package com.erp.system.accounting.service;

import com.erp.system.accounting.dto.display.AccountingDashboardDisplayDto;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.BankAccountRepository;
import com.erp.system.accounting.repository.BillRepository;
import com.erp.system.accounting.repository.BudgetRepository;
import com.erp.system.accounting.repository.JournalEntryLineRepository;
import com.erp.system.accounting.repository.JournalEntryRepository;
import com.erp.system.accounting.repository.PaymentVoucherRepository;
import com.erp.system.accounting.repository.ReceiptVoucherRepository;
import com.erp.system.common.enums.AccountingType;
import com.erp.system.common.enums.BillStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountingDashboardService {

    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final PaymentVoucherRepository paymentVoucherRepository;
    private final ReceiptVoucherRepository receiptVoucherRepository;
    private final BillRepository billRepository;
    private final BudgetRepository budgetRepository;
    private final BankAccountRepository bankAccountRepository;

    @Transactional(readOnly = true)
    public AccountingDashboardDisplayDto getDashboard() {
        LocalDate today = LocalDate.now();
        LocalDate firstDay = today.withDayOfMonth(1);
        LocalDate lastDay = today.withDayOfMonth(today.lengthOfMonth());

        BigDecimal totalRevenue = balanceFor(AccountingType.INCOME);
        BigDecimal totalExpenses = balanceFor(AccountingType.EXPENSE);
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);
        BigDecimal payablesOutstanding = billRepository.findAllByOrderByBillDateDescIdDesc().stream()
                .filter(bill -> bill.getStatus() != BillStatus.CANCELLED)
                .map(bill -> bill.getOutstandingAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal payablesPaid = billRepository.findAllByOrderByBillDateDescIdDesc().stream()
                .filter(bill -> bill.getStatus() != BillStatus.CANCELLED)
                .map(bill -> bill.getPaidAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<BigDecimal> weekDebits = new ArrayList<>(7);
        List<BigDecimal> weekCredits = new ArrayList<>(7);
        for (int daysAgo = 6; daysAgo >= 0; daysAgo--) {
            LocalDate day = today.minusDays(daysAgo);
            weekDebits.add(journalEntryLineRepository.sumDebitBetween(day, day));
            weekCredits.add(journalEntryLineRepository.sumCreditBetween(day, day));
        }

        List<BigDecimal> rollMonthDebits = new ArrayList<>(12);
        List<BigDecimal> rollMonthCredits = new ArrayList<>(12);
        YearMonth currentMonth = YearMonth.from(today);
        for (int monthsAgo = 11; monthsAgo >= 0; monthsAgo--) {
            YearMonth ym = currentMonth.minusMonths(monthsAgo);
            LocalDate monthStart = ym.atDay(1);
            LocalDate monthEnd = ym.atEndOfMonth();
            rollMonthDebits.add(journalEntryLineRepository.sumDebitBetween(monthStart, monthEnd));
            rollMonthCredits.add(journalEntryLineRepository.sumCreditBetween(monthStart, monthEnd));
        }

        return AccountingDashboardDisplayDto.builder()
                .totalAssets(balanceFor(AccountingType.ASSET))
                .totalLiabilities(balanceFor(AccountingType.LIABILITY))
                .totalEquity(balanceFor(AccountingType.EQUITY).add(netProfit))
                .totalRevenue(totalRevenue)
                .totalExpenses(totalExpenses)
                .netProfit(netProfit)
                .monthDebitTotal(journalEntryLineRepository.sumDebitBetween(firstDay, lastDay))
                .monthCreditTotal(journalEntryLineRepository.sumCreditBetween(firstDay, lastDay))
                .weekDebitSeries(weekDebits)
                .weekCreditSeries(weekCredits)
                .rollingMonthDebitSeries(rollMonthDebits)
                .rollingMonthCreditSeries(rollMonthCredits)
                .recentJournals(journalEntryRepository.findAllByOrderByEntryDateDescIdDesc().stream().limit(5).map(journal ->
                        AccountingDashboardDisplayDto.RecentDocument.builder()
                                .id(journal.getId())
                                .reference(journal.getReferenceNumber())
                                .date(journal.getEntryDate())
                                .amount(journal.getTotalDebit())
                                .status(journal.getStatus().name())
                                .build()).toList())
                .recentPayments(paymentVoucherRepository.findAllByOrderByVoucherDateDescIdDesc().stream().limit(5).map(voucher ->
                        AccountingDashboardDisplayDto.RecentDocument.builder()
                                .id(voucher.getId())
                                .reference(voucher.getReference())
                                .date(voucher.getVoucherDate())
                                .amount(voucher.getAmount())
                                .status(voucher.getStatus().name())
                                .build()).toList())
                .recentReceipts(receiptVoucherRepository.findAllByOrderByVoucherDateDescIdDesc().stream().limit(5).map(voucher ->
                        AccountingDashboardDisplayDto.RecentDocument.builder()
                                .id(voucher.getId())
                                .reference(voucher.getReference())
                                .date(voucher.getVoucherDate())
                                .amount(voucher.getAmount())
                                .status(voucher.getStatus().name())
                                .build()).toList())
                .receivablesSummary(totalAssetReceivables())
                .payablesOutstanding(payablesOutstanding)
                .payablesPaid(payablesPaid)
                .budgetSummaries(budgetRepository.findAllByOrderByBudgetYearDescBudgetMonthAscIdDesc().stream()
                        .limit(6)
                        .map(budget -> AccountingDashboardDisplayDto.BudgetSnapshot.builder()
                                .id(budget.getId())
                                .label((budget.getBudgetName() == null || budget.getBudgetName().isBlank() ? budget.getAccount().getCode() : budget.getBudgetName()))
                                .planned(budget.getPlannedAmount())
                                .actual(budget.getActualAmount())
                                .variance(budget.getPlannedAmount().subtract(budget.getActualAmount()))
                                .build())
                        .toList())
                .bankBalances(bankAccountRepository.findAllByOrderByBankNameAscAccountNumberAsc().stream()
                        .map(account -> AccountingDashboardDisplayDto.BankBalance.builder()
                                .id(account.getId())
                                .bankName(account.getBankName())
                                .accountNumber(account.getAccountNumber())
                                .balance(currentBankBalance(account))
                                .currency(account.getCurrency())
                                .build())
                        .toList())
                .build();
    }

    private BigDecimal balanceFor(AccountingType accountType) {
        BigDecimal openingBalance = switch (accountType) {
            case ASSET, LIABILITY, EQUITY -> openingBalanceFor(accountType);
            default -> BigDecimal.ZERO;
        };
        BigDecimal debit = journalEntryLineRepository.sumDebitByAccountType(accountType);
        BigDecimal credit = journalEntryLineRepository.sumCreditByAccountType(accountType);
        BigDecimal movement = switch (accountType) {
            case ASSET, EXPENSE -> debit.subtract(credit);
            case LIABILITY, EQUITY, INCOME -> credit.subtract(debit);
        };
        return openingBalance.add(movement);
    }

    private BigDecimal totalAssetReceivables() {
        return receiptVoucherRepository.findAllByOrderByVoucherDateDescIdDesc().stream()
                .filter(voucher -> voucher.getRevenueAccount() != null && voucher.getRevenueAccount().getAccountType() == AccountingType.ASSET)
                .map(voucher -> journalEntryLineRepository.sumNetMovementByAccountId(voucher.getRevenueAccount().getId()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal openingBalanceFor(AccountingType accountType) {
        return accountRepository.findByAccountTypeAndActiveTrue(accountType).stream()
                .map(account -> normalizeForDisplay(accountType, account.signedOpeningBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal normalizeForDisplay(AccountingType accountType, BigDecimal signedBalance) {
        return switch (accountType) {
            case ASSET, EXPENSE -> signedBalance;
            case LIABILITY, EQUITY, INCOME -> signedBalance.negate();
        };
    }

    private BigDecimal currentBankBalance(com.erp.system.accounting.domain.BankAccount account) {
        return (account.getOpeningBalance() == null ? BigDecimal.ZERO : account.getOpeningBalance())
                .add(journalEntryLineRepository.sumNetMovementByAccountId(account.getLinkedAccount().getId()));
    }
}
