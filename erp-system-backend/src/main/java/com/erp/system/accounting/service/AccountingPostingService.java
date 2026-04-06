package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.accounting.domain.JournalEntryLine;
import com.erp.system.accounting.support.JournalPostingNarratives;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.JournalEntryRepository;
import com.erp.system.common.enums.JournalEntryStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.service.NumberingService;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountingPostingService {

    private final JournalEntryRepository journalEntryRepository;
    private final AccountRepository accountRepository;
    private final NumberingService numberingService;
    private final PostingPeriodService postingPeriodService;

    @Transactional
    public JournalEntry createPostedJournal(LocalDate entryDate,
                                            String description,
                                            String sourceModule,
                                            Long sourceRecordId,
                                            String actor,
                                            List<JournalLineDraft> lines) {
        postingPeriodService.validatePostingDate(entryDate);
        if (lines == null || lines.size() < 2) {
            throw new BusinessException("Posted journal requires at least two lines");
        }

        JournalEntry entry = JournalEntry.builder()
                .referenceNumber(generateReferenceNumber())
                .entryDate(entryDate)
                .description(description)
                .status(JournalEntryStatus.APPROVED)
                .postedAt(LocalDateTime.now())
                .postedBy(actor)
                .sourceModule(sourceModule)
                .sourceRecordId(sourceRecordId)
                .lines(new ArrayList<>())
                .build();

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        int lineNumber = 1;

        for (JournalLineDraft draft : lines) {
            Account account = accountRepository.findById(draft.getAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account", draft.getAccountId()));
            if (!account.isActive()) {
                throw new BusinessException("Account must be active for journal posting");
            }

            BigDecimal debit = normalize(draft.getDebit());
            BigDecimal credit = normalize(draft.getCredit());
            boolean validSide = debit.compareTo(BigDecimal.ZERO) > 0 ^ credit.compareTo(BigDecimal.ZERO) > 0;
            if (!validSide) {
                throw new BusinessException("Each posted journal line must contain either a debit or a credit amount");
            }

            JournalEntryLine line = JournalEntryLine.builder()
                    .journalEntry(entry)
                    .account(account)
                    .description(draft.getDescription())
                    .debit(debit)
                    .credit(credit)
                    .lineNumber(lineNumber++)
                    .build();
            entry.getLines().add(line);
            totalDebit = totalDebit.add(debit);
            totalCredit = totalCredit.add(credit);
        }

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new BusinessException("Posted journal entry is not balanced");
        }

        entry.setTotalDebit(totalDebit);
        entry.setTotalCredit(totalCredit);
        return journalEntryRepository.save(entry);
    }

    @Transactional
    public JournalEntry reverseJournal(JournalEntry originalEntry, String actor, String reason, LocalDate reversalDate) {
        if (originalEntry == null) {
            throw new BusinessException("Journal entry is required for reversal");
        }
        if (originalEntry.getStatus() != JournalEntryStatus.APPROVED) {
            throw new BusinessException("Only approved journal entries can be reversed");
        }

        postingPeriodService.validatePostingDate(reversalDate);

        JournalEntry reversalEntry = JournalEntry.builder()
                .referenceNumber(generateReferenceNumber())
                .entryDate(reversalDate)
                .description("Reversal of " + originalEntry.getReferenceNumber() + (reason == null || reason.isBlank() ? "" : " - " + reason.trim()))
                .status(JournalEntryStatus.APPROVED)
                .postedAt(LocalDateTime.now())
                .postedBy(actor)
                .sourceModule(originalEntry.getSourceModule())
                .sourceRecordId(originalEntry.getSourceRecordId())
                .lines(new ArrayList<>())
                .build();

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        int lineNumber = 1;
        for (JournalEntryLine originalLine : originalEntry.getLines()) {
            String originalText = originalLine.getDescription();
            String reversalLineDesc = "Reversal of "
                    + (originalText != null && !originalText.isBlank()
                    ? originalText
                    : JournalPostingNarratives.accountCaption(originalLine.getAccount()));
            JournalEntryLine reversalLine = JournalEntryLine.builder()
                    .journalEntry(reversalEntry)
                    .account(originalLine.getAccount())
                    .description(reversalLineDesc)
                    .debit(originalLine.getCredit())
                    .credit(originalLine.getDebit())
                    .lineNumber(lineNumber++)
                    .build();
            reversalEntry.getLines().add(reversalLine);
            totalDebit = totalDebit.add(reversalLine.getDebit());
            totalCredit = totalCredit.add(reversalLine.getCredit());
        }

        reversalEntry.setTotalDebit(totalDebit);
        reversalEntry.setTotalCredit(totalCredit);

        originalEntry.setStatus(JournalEntryStatus.REVERSED);
        originalEntry.setReversedAt(LocalDateTime.now());
        originalEntry.setReversedBy(actor);
        originalEntry.setReversalReference(reversalEntry.getReferenceNumber());

        journalEntryRepository.save(originalEntry);
        return journalEntryRepository.save(reversalEntry);
    }

    private BigDecimal normalize(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return amount.setScale(4, RoundingMode.HALF_UP);
    }

    private String generateReferenceNumber() {
        try {
            return numberingService.generateNextNumber("JOURNAL_REFERENCE");
        } catch (Exception exception) {
            return "JE-" + System.currentTimeMillis();
        }
    }

    @Getter
    @Builder
    public static class JournalLineDraft {
        private Long accountId;
        private String description;
        private BigDecimal debit;
        private BigDecimal credit;
    }
}
