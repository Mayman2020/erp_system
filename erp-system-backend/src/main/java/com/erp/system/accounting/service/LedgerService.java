package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.JournalEntryLine;
import com.erp.system.accounting.dto.display.LedgerDisplayDto;
import com.erp.system.accounting.dto.display.LedgerLineDisplayDto;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.JournalEntryLineRepository;
import com.erp.system.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final AccountRepository accountRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;

    /**
     * Opening balance shown in the ledger comes from {@link Account#getOpeningBalance()} /
     * {@link Account#getOpeningBalanceSide()} (Chart of Accounts create/edit), in signed form via
     * {@link Account#signedOpeningBalance()}. When {@code fromDate} is set, net posted movement on the same
     * accounts strictly before that date is added so the figure is the balance at the start of the range.
     * For a parent account, openings are summed only for <strong>leaf</strong> accounts under that branch so
     * values match what is maintained on detail accounts in COA (avoids parent+child double counting).
     */
    @Transactional(readOnly = true)
    public LedgerDisplayDto getLedger(Long accountId, LocalDate fromDate, LocalDate toDate) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));

        List<Long> rollupAccountIds = resolveLedgerRollupAccountIds(account);

        BigDecimal openingBalance = sumCoaOpeningBalancesForLedger(rollupAccountIds);
        if (fromDate != null) {
            openingBalance = openingBalance.add(
                    journalEntryLineRepository.sumNetMovementBeforeAccountIds(rollupAccountIds, fromDate));
        }

        List<JournalEntryLine> ledgerLines =
                journalEntryLineRepository.findLedgerLinesForAccountIds(rollupAccountIds, fromDate, toDate);
        List<LedgerLineDisplayDto> displayLines = new ArrayList<>();
        BigDecimal runningBalance = openingBalance;

        for (JournalEntryLine line : ledgerLines) {
            runningBalance = runningBalance.add(line.getDebit()).subtract(line.getCredit());
            displayLines.add(LedgerLineDisplayDto.builder()
                    .journalEntryId(line.getJournalEntry().getId())
                    .journalReference(line.getJournalEntry().getReferenceNumber())
                    .entryDate(line.getJournalEntry().getEntryDate())
                    .lineNumber(line.getLineNumber())
                    .transactionType(line.getJournalEntry().getSourceModule() == null || line.getJournalEntry().getSourceModule().isBlank()
                            ? "JOURNAL_VOUCHER"
                            : line.getJournalEntry().getSourceModule())
                    .sourceReference(line.getJournalEntry().getExternalReference() == null || line.getJournalEntry().getExternalReference().isBlank()
                            ? line.getJournalEntry().getReferenceNumber()
                            : line.getJournalEntry().getExternalReference())
                    .description(line.getDescription())
                    .debit(line.getDebit())
                    .credit(line.getCredit())
                    .runningBalance(runningBalance)
                    .build());
        }

        return LedgerDisplayDto.builder()
                .accountId(account.getId())
                .accountCode(account.getCode())
                .accountName(account.getNameEn())
                .accountNameEn(account.getNameEn())
                .accountNameAr(account.getNameAr())
                .openingBalance(openingBalance)
                .closingBalance(runningBalance)
                .lines(displayLines)
                .build();
    }

    /**
     * Selected account always included; plus every <em>active</em> descendant matched by {@code fullPath} prefix.
     */
    private List<Long> resolveLedgerRollupAccountIds(Account root) {
        String path = root.getFullPath();
        if (path == null || path.isBlank()) {
            return List.of(root.getId());
        }
        List<Long> ids = accountRepository.findLedgerSubtreeAccountIds(root.getId(), path);
        LinkedHashSet<Long> unique = new LinkedHashSet<>(ids);
        return unique.stream().collect(Collectors.toList());
    }

    private BigDecimal sumCoaOpeningBalancesForLedger(List<Long> rollupAccountIds) {
        if (rollupAccountIds == null || rollupAccountIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return accountRepository.findLedgerLeafAccountsAmongIds(rollupAccountIds).stream()
                .map(Account::signedOpeningBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
