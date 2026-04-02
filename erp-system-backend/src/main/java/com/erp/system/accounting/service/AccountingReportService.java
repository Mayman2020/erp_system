package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.accounting.domain.JournalEntryLine;
import com.erp.system.accounting.dto.display.BalanceSheetLineDto;
import com.erp.system.accounting.dto.display.BalanceSheetReportDto;
import com.erp.system.accounting.dto.display.ProfitLossLineDto;
import com.erp.system.accounting.dto.display.ProfitLossReportDto;
import com.erp.system.accounting.repository.JournalEntryRepository;
import com.erp.system.common.enums.AccountingType;
import com.erp.system.common.enums.JournalEntryStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountingReportService {

    private final JournalEntryRepository journalEntryRepository;

    @Transactional(readOnly = true)
    public ProfitLossReportDto getProfitLoss(LocalDate fromDate, LocalDate toDate) {
        List<JournalEntry> entries = journalEntryRepository.searchJournalEntries(
                JournalEntryStatus.POSTED,
                fromDate,
                toDate,
                null
        );

        Map<Long, ProfitLossLineDto> revenues = new LinkedHashMap<>();
        Map<Long, ProfitLossLineDto> expenses = new LinkedHashMap<>();
        for (JournalEntry entry : entries) {
            for (JournalEntryLine line : entry.getLines()) {
                Account account = line.getAccount();
                if (account == null) {
                    continue;
                }
                if (account.getAccountType() == AccountingType.INCOME) {
                    accumulateProfitLoss(revenues, account, line.getCredit().subtract(line.getDebit()));
                } else if (account.getAccountType() == AccountingType.EXPENSE) {
                    accumulateProfitLoss(expenses, account, line.getDebit().subtract(line.getCredit()));
                }
            }
        }

        List<ProfitLossLineDto> revenueLines = revenues.values().stream()
                .sorted(Comparator.comparing(ProfitLossLineDto::getAccountCode))
                .toList();
        List<ProfitLossLineDto> expenseLines = expenses.values().stream()
                .sorted(Comparator.comparing(ProfitLossLineDto::getAccountCode))
                .toList();
        BigDecimal totalRevenue = revenueLines.stream()
                .map(ProfitLossLineDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalExpenses = expenseLines.stream()
                .map(ProfitLossLineDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        return ProfitLossReportDto.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .revenues(revenueLines)
                .expenses(expenseLines)
                .totalRevenue(totalRevenue)
                .totalExpenses(totalExpenses)
                .netProfit(totalRevenue.subtract(totalExpenses).setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    @Transactional(readOnly = true)
    public BalanceSheetReportDto getBalanceSheet(LocalDate asOfDate) {
        List<JournalEntry> entries = journalEntryRepository.searchJournalEntries(
                JournalEntryStatus.POSTED,
                null,
                asOfDate,
                null
        );

        Map<Long, BalanceSheetLineDto> assets = new LinkedHashMap<>();
        Map<Long, BalanceSheetLineDto> liabilities = new LinkedHashMap<>();
        Map<Long, BalanceSheetLineDto> equity = new LinkedHashMap<>();
        for (JournalEntry entry : entries) {
            for (JournalEntryLine line : entry.getLines()) {
                Account account = line.getAccount();
                if (account == null) {
                    continue;
                }
                switch (account.getAccountType()) {
                    case ASSET -> accumulateBalance(assets, account, line.getDebit().subtract(line.getCredit()));
                    case LIABILITY -> accumulateBalance(liabilities, account, line.getCredit().subtract(line.getDebit()));
                    case EQUITY -> accumulateBalance(equity, account, line.getCredit().subtract(line.getDebit()));
                    default -> {
                        // P&L accounts are not shown directly in balance sheet detail.
                    }
                }
            }
        }

        List<BalanceSheetLineDto> assetLines = sortedBalanceLines(assets);
        List<BalanceSheetLineDto> liabilityLines = sortedBalanceLines(liabilities);
        List<BalanceSheetLineDto> equityLines = sortedBalanceLines(equity);

        BigDecimal totalAssets = total(assetLines);
        BigDecimal totalLiabilities = total(liabilityLines);
        BigDecimal totalEquity = total(equityLines);
        BigDecimal liabilitiesAndEquity = totalLiabilities.add(totalEquity).setScale(2, RoundingMode.HALF_UP);

        return BalanceSheetReportDto.builder()
                .asOfDate(asOfDate)
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

    private void accumulateProfitLoss(Map<Long, ProfitLossLineDto> target, Account account, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        ProfitLossLineDto current = target.computeIfAbsent(account.getId(), accountId -> ProfitLossLineDto.builder()
                .accountId(accountId)
                .accountCode(account.getCode())
                .accountNameEn(account.getNameEn())
                .accountNameAr(account.getNameAr())
                .amount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .build());
        current.setAmount(current.getAmount().add(amount).setScale(2, RoundingMode.HALF_UP));
    }

    private void accumulateBalance(Map<Long, BalanceSheetLineDto> target, Account account, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        BalanceSheetLineDto current = target.computeIfAbsent(account.getId(), accountId -> BalanceSheetLineDto.builder()
                .accountId(accountId)
                .accountCode(account.getCode())
                .accountNameEn(account.getNameEn())
                .accountNameAr(account.getNameAr())
                .balance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .build());
        current.setBalance(current.getBalance().add(amount).setScale(2, RoundingMode.HALF_UP));
    }

    private List<BalanceSheetLineDto> sortedBalanceLines(Map<Long, BalanceSheetLineDto> source) {
        List<BalanceSheetLineDto> lines = new ArrayList<>(source.values());
        lines.sort(Comparator.comparing(BalanceSheetLineDto::getAccountCode));
        return lines;
    }

    private BigDecimal total(List<BalanceSheetLineDto> lines) {
        return lines.stream()
                .map(BalanceSheetLineDto::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
