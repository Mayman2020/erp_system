package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.accounting.domain.JournalEntryLine;
import com.erp.system.accounting.dto.display.JournalEntryDisplayDto;
import com.erp.system.accounting.dto.form.JournalEntryFormDto;
import com.erp.system.accounting.dto.form.JournalEntryLineFormDto;
import com.erp.system.accounting.mapper.JournalEntryMapper;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.JournalEntryRepository;
import com.erp.system.common.enums.JournalEntryStatus;
import com.erp.system.accounting.service.PostingPeriodService;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.service.NumberingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryMapper journalEntryMapper;
    private final NumberingService numberingService;
    private final PostingPeriodService postingPeriodService;
    private final AccountingAuditService auditService;

    @Transactional(readOnly = true)
    public List<JournalEntryDisplayDto> getJournalEntries() {
        return journalEntryRepository.findAllByOrderByEntryDateDescIdDesc().stream()
                .map(this::toDisplay)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<JournalEntryDisplayDto> getJournalEntries(Pageable pageable) {
        return journalEntryRepository.findAllByOrderByEntryDateDescIdDesc(pageable)
                .map(this::toDisplay);
    }

    @Transactional(readOnly = true)
    public List<JournalEntryDisplayDto> searchJournalEntries(String search,
                                                             JournalEntryStatus status,
                                                             LocalDate fromDate,
                                                             LocalDate toDate,
                                                             Long accountId) {
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim().toLowerCase(Locale.ROOT);
        return journalEntryRepository.searchJournalEntries(status, fromDate, toDate, accountId).stream()
                .filter(entry -> matchesSearch(entry, normalizedSearch))
                .map(this::toDisplay)
                .collect(Collectors.toList());
    }

    private boolean matchesSearch(JournalEntry entry, String normalizedSearch) {
        if (normalizedSearch == null) {
            return true;
        }
        String ref = entry.getReferenceNumber() == null ? "" : entry.getReferenceNumber().toLowerCase(Locale.ROOT);
        String desc = entry.getDescription() == null ? "" : entry.getDescription().toLowerCase(Locale.ROOT);
        return ref.contains(normalizedSearch) || desc.contains(normalizedSearch);
    }

    @Transactional(readOnly = true)
    public JournalEntryDisplayDto getJournalEntry(Long id) {
        JournalEntry journalEntry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", id));
        return toDisplay(journalEntry);
    }

    @Transactional
    public JournalEntryDisplayDto createJournalEntry(JournalEntryFormDto request) {
        validateJournalEntryForm(request, null);

        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setReferenceNumber(generateReferenceNumber());
        journalEntry.setEntryDate(request.getEntryDate());
        journalEntry.setDescription(request.getDescription());
        journalEntry.setExternalReference(request.getExternalReference());
        journalEntry.setCurrencyCode(request.getCurrencyCode() == null || request.getCurrencyCode().isBlank() ? "USD" : request.getCurrencyCode().trim().toUpperCase(Locale.ROOT));
        journalEntry.setEntryType(request.getEntryType() == null || request.getEntryType().isBlank() ? "MANUAL" : request.getEntryType().trim().toUpperCase(Locale.ROOT));
        journalEntry.setStatus(JournalEntryStatus.DRAFT);
        journalEntry.setLines(new ArrayList<>());

        // Process lines and calculate totals
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        int lineNumber = 1;

        for (JournalEntryLineFormDto lineRequest : request.getLines()) {
            Account account = accountRepository.findById(lineRequest.getAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account", lineRequest.getAccountId()));

            if (!account.isActive()) {
                throw new BusinessException("Cannot use inactive account: " + account.getCode());
            }

            if (!account.isPostable()) {
                throw new BusinessException("Cannot post to non-postable account: " + account.getCode());
            }

            JournalEntryLine line = new JournalEntryLine();
            line.setJournalEntry(journalEntry);
            line.setAccount(account);
            line.setDescription(lineRequest.getDescription());
            line.setDebit(lineRequest.getDebit().setScale(4, RoundingMode.HALF_UP));
            line.setCredit(lineRequest.getCredit().setScale(4, RoundingMode.HALF_UP));
            line.setLineNumber(lineNumber++);

            journalEntry.getLines().add(line);

            totalDebit = totalDebit.add(line.getDebit());
            totalCredit = totalCredit.add(line.getCredit());
        }

        // Validate totals match
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new BusinessException("Journal entry is not balanced. Debit: " + totalDebit + ", Credit: " + totalCredit);
        }

        journalEntry.setTotalDebit(totalDebit);
        journalEntry.setTotalCredit(totalCredit);

        JournalEntry saved = journalEntryRepository.save(journalEntry);
        return toDisplay(saved);
    }

    @Transactional
    public JournalEntryDisplayDto updateJournalEntry(Long id, JournalEntryFormDto request) {
        JournalEntry journalEntry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", id));

        if (journalEntry.getStatus() != JournalEntryStatus.DRAFT) {
            throw new BusinessException("Only draft journal entries can be updated");
        }

        validateJournalEntryForm(request, id);

        journalEntry.setEntryDate(request.getEntryDate());
        journalEntry.setDescription(request.getDescription());
        journalEntry.setExternalReference(request.getExternalReference());
        journalEntry.setCurrencyCode(request.getCurrencyCode() == null || request.getCurrencyCode().isBlank() ? "USD" : request.getCurrencyCode().trim().toUpperCase(Locale.ROOT));
        journalEntry.setEntryType(request.getEntryType() == null || request.getEntryType().isBlank() ? "MANUAL" : request.getEntryType().trim().toUpperCase(Locale.ROOT));

        // Clear existing lines
        journalEntry.getLines().clear();

        // Process new lines
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        int lineNumber = 1;

        for (JournalEntryLineFormDto lineRequest : request.getLines()) {
            Account account = accountRepository.findById(lineRequest.getAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account", lineRequest.getAccountId()));

            if (!account.isActive()) {
                throw new BusinessException("Cannot use inactive account: " + account.getCode());
            }

            if (!account.isPostable()) {
                throw new BusinessException("Cannot post to non-postable account: " + account.getCode());
            }

            JournalEntryLine line = new JournalEntryLine();
            line.setJournalEntry(journalEntry);
            line.setAccount(account);
            line.setDescription(lineRequest.getDescription());
            line.setDebit(lineRequest.getDebit().setScale(4, RoundingMode.HALF_UP));
            line.setCredit(lineRequest.getCredit().setScale(4, RoundingMode.HALF_UP));
            line.setLineNumber(lineNumber++);

            journalEntry.getLines().add(line);

            totalDebit = totalDebit.add(line.getDebit());
            totalCredit = totalCredit.add(line.getCredit());
        }

        // Validate totals match
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new BusinessException("Journal entry is not balanced. Debit: " + totalDebit + ", Credit: " + totalCredit);
        }

        journalEntry.setTotalDebit(totalDebit);
        journalEntry.setTotalCredit(totalCredit);

        JournalEntry saved = journalEntryRepository.save(journalEntry);
        return toDisplay(saved);
    }

    @Transactional
    public JournalEntryDisplayDto postJournalEntry(Long id, String postedBy) {
        JournalEntry journalEntry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", id));

        if (journalEntry.getStatus() != JournalEntryStatus.DRAFT) {
            throw new BusinessException("Only draft journal entries can be posted");
        }

        postingPeriodService.validatePostingDate(journalEntry.getEntryDate());

        if (journalEntry.getTotalDebit().compareTo(journalEntry.getTotalCredit()) != 0) {
            throw new BusinessException("Cannot post unbalanced journal entry");
        }

        journalEntry.setStatus(JournalEntryStatus.POSTED);
        journalEntry.setPostedAt(LocalDateTime.now());
        journalEntry.setPostedBy(postedBy);

        JournalEntry saved = journalEntryRepository.save(journalEntry);
        auditService.log("JournalEntry", saved.getId(), "POST", postedBy, "Posted journal " + saved.getReferenceNumber());
        return toDisplay(saved);
    }

    @Transactional
    public JournalEntryDisplayDto reverseJournalEntry(Long id, String reversedBy, String reason) {
        JournalEntry originalEntry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", id));

        if (originalEntry.getStatus() != JournalEntryStatus.POSTED) {
            throw new BusinessException("Only posted journal entries can be reversed");
        }

        // Create reversal entry
        JournalEntry reversalEntry = new JournalEntry();
        reversalEntry.setReferenceNumber(generateReferenceNumber());
        reversalEntry.setEntryDate(LocalDate.now());
        reversalEntry.setDescription("Reversal of " + originalEntry.getReferenceNumber() +
                                   (reason != null ? ": " + reason : ""));
        reversalEntry.setExternalReference(originalEntry.getExternalReference());
        reversalEntry.setCurrencyCode(originalEntry.getCurrencyCode());
        reversalEntry.setEntryType("REVERSAL");
        reversalEntry.setStatus(JournalEntryStatus.POSTED);
        reversalEntry.setPostedAt(LocalDateTime.now());
        reversalEntry.setPostedBy(reversedBy);
        reversalEntry.setLines(new ArrayList<>());

        // Reverse each line
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        int lineNumber = 1;

        for (JournalEntryLine originalLine : originalEntry.getLines()) {
            JournalEntryLine reversalLine = new JournalEntryLine();
            reversalLine.setJournalEntry(reversalEntry);
            reversalLine.setAccount(originalLine.getAccount());
            reversalLine.setDescription("Reversal of " + (originalLine.getDescription() != null ? originalLine.getDescription() : ""));
            reversalLine.setDebit(originalLine.getCredit()); // Swap debit and credit
            reversalLine.setCredit(originalLine.getDebit());
            reversalLine.setLineNumber(lineNumber++);

            reversalEntry.getLines().add(reversalLine);

            totalDebit = totalDebit.add(reversalLine.getDebit());
            totalCredit = totalCredit.add(reversalLine.getCredit());
        }

        reversalEntry.setTotalDebit(totalDebit);
        reversalEntry.setTotalCredit(totalCredit);

        // Mark original as reversed
        originalEntry.setStatus(JournalEntryStatus.REVERSED);
        originalEntry.setReversedAt(LocalDateTime.now());
        originalEntry.setReversedBy(reversedBy);
        originalEntry.setReversalReference(reversalEntry.getReferenceNumber());

        journalEntryRepository.save(originalEntry);
        JournalEntry savedReversal = journalEntryRepository.save(reversalEntry);
        auditService.log("JournalEntry", originalEntry.getId(), "REVERSE", reversedBy, "Reversed journal " + originalEntry.getReferenceNumber() + " → " + savedReversal.getReferenceNumber());

        return toDisplay(savedReversal);
    }

    @Transactional
    public void deleteJournalEntry(Long id) {
        JournalEntry journalEntry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", id));

        if (journalEntry.getStatus() != JournalEntryStatus.DRAFT) {
            throw new BusinessException("Only draft journal entries can be deleted");
        }

        journalEntryRepository.delete(journalEntry);
    }

    private void validateJournalEntryForm(JournalEntryFormDto request, Long excludeId) {
        if (request.getEntryDate() == null) {
            throw new BusinessException("Entry date is required");
        }

        if (request.getLines() == null || request.getLines().isEmpty()) {
            throw new BusinessException("Journal entry must have at least one line");
        }

        if (request.getLines().size() < 2) {
            throw new BusinessException("Journal entry must have at least two lines");
        }
        for (JournalEntryLineFormDto line : request.getLines()) {
            boolean hasDebit = line.getDebit() != null && line.getDebit().compareTo(BigDecimal.ZERO) > 0;
            boolean hasCredit = line.getCredit() != null && line.getCredit().compareTo(BigDecimal.ZERO) > 0;
            if (!(hasDebit ^ hasCredit)) {
                throw new BusinessException("Each journal line must contain either a debit or a credit amount");
            }
        }
    }

    @Transactional
    public JournalEntryDisplayDto cancelJournalEntry(Long id) {
        JournalEntry journalEntry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", id));

        if (journalEntry.getStatus() != JournalEntryStatus.DRAFT) {
            throw new BusinessException("Only draft journal entries can be cancelled");
        }

        journalEntry.setStatus(JournalEntryStatus.CANCELLED);
        JournalEntry saved = journalEntryRepository.save(journalEntry);
        auditService.log("JournalEntry", journalEntry.getId(), "CANCEL", "system", "Cancelled journal " + journalEntry.getReferenceNumber());
        return toDisplay(saved);
    }

    private String generateReferenceNumber() {
        try {
            return numberingService.generateNextNumber("JOURNAL_REFERENCE");
        } catch (Exception e) {
            // Fallback
            return "JE-" + System.currentTimeMillis();
        }
    }

    private JournalEntryDisplayDto toDisplay(JournalEntry journalEntry) {
        JournalEntryDisplayDto dto = journalEntryMapper.toDisplay(journalEntry);
        dto.setBalanced(journalEntry.getTotalDebit().compareTo(journalEntry.getTotalCredit()) == 0);
        return dto;
    }
}
