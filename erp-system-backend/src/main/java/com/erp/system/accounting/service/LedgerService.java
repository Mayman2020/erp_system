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
import java.util.List;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final AccountRepository accountRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;

    @Transactional(readOnly = true)
    public LedgerDisplayDto getLedger(Long accountId, LocalDate fromDate, LocalDate toDate) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));

        BigDecimal openingBalance = account.signedOpeningBalance();
        if (fromDate != null) {
            openingBalance = openingBalance.add(journalEntryLineRepository.sumNetMovementBefore(accountId, fromDate));
        }

        List<JournalEntryLine> ledgerLines = journalEntryLineRepository.findLedgerLines(accountId, fromDate, toDate);
        List<LedgerLineDisplayDto> displayLines = new ArrayList<>();
        BigDecimal runningBalance = openingBalance;

        for (JournalEntryLine line : ledgerLines) {
            runningBalance = runningBalance.add(line.getDebit()).subtract(line.getCredit());
            displayLines.add(LedgerLineDisplayDto.builder()
                    .journalEntryId(line.getJournalEntry().getId())
                    .journalReference(line.getJournalEntry().getReferenceNumber())
                    .entryDate(line.getJournalEntry().getEntryDate())
                    .lineNumber(line.getLineNumber())
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
                .openingBalance(openingBalance)
                .closingBalance(runningBalance)
                .lines(displayLines)
                .build();
    }
}
