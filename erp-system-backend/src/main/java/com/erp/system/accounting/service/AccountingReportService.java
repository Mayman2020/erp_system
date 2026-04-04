package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.dto.display.BalanceSheetLineDto;
import com.erp.system.accounting.dto.display.BalanceSheetReportDto;
import com.erp.system.accounting.dto.display.ProfitLossLineDto;
import com.erp.system.accounting.dto.display.ProfitLossReportDto;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.JournalEntryLineRepository;
import com.erp.system.common.enums.AccountingType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AccountingReportService {

    private static final Long CURRENT_EARNINGS_ACCOUNT_ID = -1L;
    private static final String CURRENT_EARNINGS_CODE = "3999";
    private static final String CURRENT_EARNINGS_NAME_EN = "Current Period Earnings";
    private static final String CURRENT_EARNINGS_NAME_AR = "أرباح الفترة الحالية";

    private final AccountRepository accountRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final CurrencyConversionService currencyConversionService;

    // ===================== PROFIT & LOSS =====================

    @Transactional(readOnly = true)
    public ProfitLossReportDto getProfitLoss(LocalDate fromDate, LocalDate toDate) {
        return getProfitLoss(fromDate, toDate, null);
    }

    @Transactional(readOnly = true)
    public ProfitLossReportDto getProfitLoss(LocalDate fromDate, LocalDate toDate, String reportCurrency) {
        List<ProfitLossLineDto> revenueLines;
        List<ProfitLossLineDto> expenseLines;

        if (reportCurrency != null && !reportCurrency.isBlank()) {
            String rc = reportCurrency.trim().toUpperCase(Locale.ROOT);
            revenueLines = aggregateWithConversion(fromDate, toDate, rc, true);
            expenseLines = aggregateWithConversion(fromDate, toDate, rc, false);
        } else {
            revenueLines = journalEntryLineRepository.aggregateRevenues(fromDate, toDate);
            expenseLines = journalEntryLineRepository.aggregateExpenses(fromDate, toDate);
        }

        BigDecimal totalRevenue = sumAmounts(revenueLines.stream().map(ProfitLossLineDto::getAmount).toList());
        BigDecimal totalExpenses = sumAmounts(expenseLines.stream().map(ProfitLossLineDto::getAmount).toList());

        return ProfitLossReportDto.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .reportCurrency(reportCurrency)
                .revenues(revenueLines)
                .expenses(expenseLines)
                .totalRevenue(totalRevenue)
                .totalExpenses(totalExpenses)
                .netProfit(totalRevenue.subtract(totalExpenses).setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    private List<ProfitLossLineDto> aggregateWithConversion(LocalDate fromDate, LocalDate toDate,
                                                            String reportCurrency, boolean isRevenue) {
        List<String> currencies = journalEntryLineRepository.findDistinctCurrencyCodes();
        Map<Long, ProfitLossLineDto> merged = new LinkedHashMap<>();

        for (String currency : currencies) {
            List<ProfitLossLineDto> lines = isRevenue
                    ? journalEntryLineRepository.aggregateRevenuesByCurrency(fromDate, toDate, currency)
                    : journalEntryLineRepository.aggregateExpensesByCurrency(fromDate, toDate, currency);

            LocalDate conversionDate = toDate != null ? toDate : LocalDate.now();
            BigDecimal rate = safeRate(currency, reportCurrency, conversionDate);

            for (ProfitLossLineDto line : lines) {
                BigDecimal converted = line.getAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
                merged.merge(line.getAccountId(), ProfitLossLineDto.builder()
                        .accountId(line.getAccountId())
                        .accountCode(line.getAccountCode())
                        .accountNameEn(line.getAccountNameEn())
                        .accountNameAr(line.getAccountNameAr())
                        .amount(converted)
                        .build(), (existing, incoming) -> {
                    existing.setAmount(existing.getAmount().add(incoming.getAmount()).setScale(2, RoundingMode.HALF_UP));
                    return existing;
                });
            }
        }

        List<ProfitLossLineDto> result = new ArrayList<>(merged.values());
        result.sort(Comparator.comparing(ProfitLossLineDto::getAccountCode));
        return result;
    }

    // ===================== BALANCE SHEET =====================

    @Transactional(readOnly = true)
    public BalanceSheetReportDto getBalanceSheet(LocalDate asOfDate) {
        return getBalanceSheet(asOfDate, null);
    }

    @Transactional(readOnly = true)
    public BalanceSheetReportDto getBalanceSheet(LocalDate asOfDate, String reportCurrency) {
        boolean convertCurrency = reportCurrency != null && !reportCurrency.isBlank();
        String rc = convertCurrency ? reportCurrency.trim().toUpperCase(Locale.ROOT) : null;

        Map<Long, BalanceSheetLineDto> assets = new LinkedHashMap<>();
        Map<Long, BalanceSheetLineDto> liabilities = new LinkedHashMap<>();
        Map<Long, BalanceSheetLineDto> equity = new LinkedHashMap<>();

        loadOpeningBalances(assets, liabilities, equity);

        mergeTransactionMovements(assets, AccountingType.ASSET, asOfDate, rc, false);
        mergeTransactionMovements(liabilities, AccountingType.LIABILITY, asOfDate, rc, true);
        mergeTransactionMovements(equity, AccountingType.EQUITY, asOfDate, rc, true);

        BigDecimal currentEarnings = computeCurrentEarnings(asOfDate, rc);
        if (currentEarnings.compareTo(BigDecimal.ZERO) != 0) {
            accumulateSyntheticEquity(equity, currentEarnings);
        }

        List<BalanceSheetLineDto> assetLines = sortedValues(assets);
        List<BalanceSheetLineDto> liabilityLines = sortedValues(liabilities);
        List<BalanceSheetLineDto> equityLines = sortedValues(equity);

        BigDecimal totalAssets = sumBalances(assetLines);
        BigDecimal totalLiabilities = sumBalances(liabilityLines);
        BigDecimal totalEquity = sumBalances(equityLines);
        BigDecimal liabilitiesAndEquity = totalLiabilities.add(totalEquity).setScale(2, RoundingMode.HALF_UP);

        return BalanceSheetReportDto.builder()
                .asOfDate(asOfDate)
                .reportCurrency(reportCurrency)
                .assets(assetLines)
                .liabilities(liabilityLines)
                .equity(equityLines)
                .totalAssets(totalAssets)
                .totalLiabilities(totalLiabilities)
                .totalEquity(totalEquity)
                .liabilitiesAndEquity(liabilitiesAndEquity)
                .balanced(totalAssets.compareTo(liabilitiesAndEquity) == 0)
                .build();
    }

    private void loadOpeningBalances(Map<Long, BalanceSheetLineDto> assets,
                                     Map<Long, BalanceSheetLineDto> liabilities,
                                     Map<Long, BalanceSheetLineDto> equity) {
        List<Account> accounts = accountRepository.findAllByOrderByCodeAsc();
        for (Account account : accounts) {
            if (account.getOpeningBalance() == null || account.getOpeningBalance().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            BigDecimal displayAmount = normalizeForDisplay(account, account.signedOpeningBalance());
            switch (account.getAccountType()) {
                case ASSET -> mergeBalanceLine(assets, account, displayAmount);
                case LIABILITY -> mergeBalanceLine(liabilities, account, displayAmount);
                case EQUITY -> mergeBalanceLine(equity, account, displayAmount);
                default -> { }
            }
        }
    }

    private void mergeTransactionMovements(Map<Long, BalanceSheetLineDto> target,
                                           AccountingType accountType, LocalDate asOfDate,
                                           String reportCurrency, boolean negateSign) {
        if (reportCurrency != null) {
            List<String> currencies = journalEntryLineRepository.findDistinctCurrencyCodes();
            for (String currency : currencies) {
                List<BalanceSheetLineDto> lines = journalEntryLineRepository
                        .aggregateBalanceByTypeAndCurrency(accountType, asOfDate, currency);
                BigDecimal rate = safeRate(currency, reportCurrency, asOfDate != null ? asOfDate : LocalDate.now());
                for (BalanceSheetLineDto line : lines) {
                    BigDecimal raw = line.getBalance().multiply(rate).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal displayAmount = negateSign ? raw.negate() : raw;
                    mergeBalanceLine(target, line, displayAmount);
                }
            }
        } else {
            List<BalanceSheetLineDto> lines = journalEntryLineRepository
                    .aggregateBalanceByType(accountType, asOfDate);
            for (BalanceSheetLineDto line : lines) {
                BigDecimal displayAmount = negateSign ? line.getBalance().negate() : line.getBalance();
                mergeBalanceLine(target, line, displayAmount);
            }
        }
    }

    private BigDecimal computeCurrentEarnings(LocalDate asOfDate, String reportCurrency) {
        if (reportCurrency != null) {
            List<String> currencies = journalEntryLineRepository.findDistinctCurrencyCodes();
            BigDecimal totalRevenue = BigDecimal.ZERO;
            BigDecimal totalExpenses = BigDecimal.ZERO;
            LocalDate convDate = asOfDate != null ? asOfDate : LocalDate.now();

            for (String currency : currencies) {
                BigDecimal rate = safeRate(currency, reportCurrency, convDate);
                List<ProfitLossLineDto> rev = journalEntryLineRepository
                        .aggregateRevenuesByCurrency(null, asOfDate, currency);
                for (ProfitLossLineDto r : rev) {
                    totalRevenue = totalRevenue.add(r.getAmount().multiply(rate));
                }
                List<ProfitLossLineDto> exp = journalEntryLineRepository
                        .aggregateExpensesByCurrency(null, asOfDate, currency);
                for (ProfitLossLineDto e : exp) {
                    totalExpenses = totalExpenses.add(e.getAmount().multiply(rate));
                }
            }
            return totalRevenue.subtract(totalExpenses).setScale(2, RoundingMode.HALF_UP);
        } else {
            BigDecimal revenue = journalEntryLineRepository.sumNetIncomeUpTo(asOfDate);
            BigDecimal expenses = journalEntryLineRepository.sumNetExpenseUpTo(asOfDate);
            return revenue.subtract(expenses).setScale(2, RoundingMode.HALF_UP);
        }
    }

    // ===================== HELPERS =====================

    private BigDecimal normalizeForDisplay(Account account, BigDecimal signedBalance) {
        return switch (account.getAccountType()) {
            case ASSET, EXPENSE -> signedBalance;
            case LIABILITY, EQUITY, REVENUE -> signedBalance.negate();
        };
    }

    private void mergeBalanceLine(Map<Long, BalanceSheetLineDto> target, Account account, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        target.merge(account.getId(), BalanceSheetLineDto.builder()
                .accountId(account.getId())
                .accountCode(account.getCode())
                .accountNameEn(account.getNameEn())
                .accountNameAr(account.getNameAr())
                .balance(amount.setScale(2, RoundingMode.HALF_UP))
                .build(), (existing, incoming) -> {
            existing.setBalance(existing.getBalance().add(incoming.getBalance()).setScale(2, RoundingMode.HALF_UP));
            return existing;
        });
    }

    private void mergeBalanceLine(Map<Long, BalanceSheetLineDto> target, BalanceSheetLineDto dto, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        target.merge(dto.getAccountId(), BalanceSheetLineDto.builder()
                .accountId(dto.getAccountId())
                .accountCode(dto.getAccountCode())
                .accountNameEn(dto.getAccountNameEn())
                .accountNameAr(dto.getAccountNameAr())
                .balance(amount.setScale(2, RoundingMode.HALF_UP))
                .build(), (existing, incoming) -> {
            existing.setBalance(existing.getBalance().add(incoming.getBalance()).setScale(2, RoundingMode.HALF_UP));
            return existing;
        });
    }

    private void accumulateSyntheticEquity(Map<Long, BalanceSheetLineDto> target, BigDecimal amount) {
        target.merge(CURRENT_EARNINGS_ACCOUNT_ID, BalanceSheetLineDto.builder()
                .accountId(CURRENT_EARNINGS_ACCOUNT_ID)
                .accountCode(CURRENT_EARNINGS_CODE)
                .accountNameEn(CURRENT_EARNINGS_NAME_EN)
                .accountNameAr(CURRENT_EARNINGS_NAME_AR)
                .balance(amount.setScale(2, RoundingMode.HALF_UP))
                .build(), (existing, incoming) -> {
            existing.setBalance(existing.getBalance().add(incoming.getBalance()).setScale(2, RoundingMode.HALF_UP));
            return existing;
        });
    }

    private BigDecimal safeRate(String fromCurrency, String toCurrency, LocalDate date) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return BigDecimal.ONE;
        }
        try {
            return currencyConversionService.getRate(fromCurrency, toCurrency, date);
        } catch (Exception e) {
            return BigDecimal.ONE;
        }
    }

    private List<BalanceSheetLineDto> sortedValues(Map<Long, BalanceSheetLineDto> source) {
        List<BalanceSheetLineDto> lines = new ArrayList<>(source.values());
        lines.sort(Comparator.comparing(BalanceSheetLineDto::getAccountCode));
        return lines;
    }

    private BigDecimal sumBalances(List<BalanceSheetLineDto> lines) {
        return lines.stream()
                .map(BalanceSheetLineDto::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumAmounts(List<BigDecimal> amounts) {
        return amounts.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
