package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.accounting.domain.Transfer;
import com.erp.system.accounting.dto.display.TransferDisplayDto;
import com.erp.system.accounting.dto.form.TransferFormDto;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.JournalEntryRepository;
import com.erp.system.accounting.repository.TransferRepository;
import com.erp.system.accounting.support.JournalPostingNarratives;
import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.service.NumberingService;
import com.erp.system.erp.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferService {

    private static final String MODULE = "ACCOUNTING";

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final NumberingService numberingService;
    private final AccountingPostingService accountingPostingService;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<TransferDisplayDto> getAll() {
        return transferRepository.findAllByOrderByTransferDateDescIdDesc().stream()
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransferDisplayDto getById(Long id) {
        return toDisplay(loadTransfer(id));
    }

    @Transactional
    public TransferDisplayDto create(TransferFormDto request) {
        Transfer transfer = new Transfer();
        transfer.setReference(resolveReference(request.getReference()));
        transfer.setStatus(TransactionStatus.DRAFT);
        applyForm(transfer, request);
        transfer = transferRepository.save(transfer);
        activityLogService.log(MODULE, "CREATE", "Transfer", transfer.getId(), transfer.getReference(),
                "Created transfer " + transfer.getReference());
        return toDisplay(transfer);
    }

    @Transactional
    public TransferDisplayDto update(Long id, TransferFormDto request) {
        Transfer transfer = loadTransfer(id);
        if (transfer.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft transfers can be edited");
        }
        String requestedReference = normalizeOptional(request.getReference());
        if (requestedReference != null && !requestedReference.equalsIgnoreCase(transfer.getReference())) {
            if (transferRepository.existsByReferenceIgnoreCase(requestedReference)) {
                throw new BusinessException("Transfer reference already exists");
            }
            transfer.setReference(requestedReference);
        }
        applyForm(transfer, request);
        transfer = transferRepository.save(transfer);
        activityLogService.log(MODULE, "UPDATE", "Transfer", transfer.getId(), transfer.getReference(),
                "Updated transfer " + transfer.getReference());
        return toDisplay(transfer);
    }

    @Transactional
    public TransferDisplayDto post(Long id, String actor) {
        Transfer transfer = loadTransfer(id);
        if (transfer.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Cancelled transfer cannot be posted");
        }
        if (transfer.getStatus() == TransactionStatus.POSTED) {
            return toDisplay(transfer);
        }

        Account source = loadActiveAccount(transfer.getSourceAccountId());
        Account destination = loadActiveAccount(transfer.getDestinationAccountId());
        String narrative = JournalPostingNarratives.entryHeader(
                transfer.getDescription(),
                "Transfer",
                transfer.getReference()
        );

        JournalEntry journalEntry = accountingPostingService.createPostedJournal(
                transfer.getTransferDate(),
                narrative,
                "TRANSFER",
                transfer.getId(),
                actor,
                List.of(
                        AccountingPostingService.JournalLineDraft.builder()
                                .accountId(destination.getId())
                                .description(JournalPostingNarratives.lineWithAccount(narrative, destination, true))
                                .debit(transfer.getAmount())
                                .credit(BigDecimal.ZERO)
                                .build(),
                        AccountingPostingService.JournalLineDraft.builder()
                                .accountId(source.getId())
                                .description(JournalPostingNarratives.lineWithAccount(narrative, source, false))
                                .debit(BigDecimal.ZERO)
                                .credit(transfer.getAmount())
                                .build()
                )
        );

        transfer.setStatus(TransactionStatus.POSTED);
        transfer.setPostedAt(LocalDateTime.now());
        transfer.setPostedBy(actor);
        transfer.setJournalEntryId(journalEntry.getId());
        transfer = transferRepository.save(transfer);
        activityLogService.log(MODULE, "POST", "Transfer", transfer.getId(), transfer.getReference(),
                "Posted transfer " + transfer.getReference());
        return toDisplay(transfer);
    }

    @Transactional
    public TransferDisplayDto cancel(Long id, String actor, String reason) {
        Transfer transfer = loadTransfer(id);
        if (transfer.getStatus() == TransactionStatus.CANCELLED) {
            return toDisplay(transfer);
        }
        if (transfer.getJournalEntryId() != null) {
            final Long journalEntryId = transfer.getJournalEntryId();
            JournalEntry original = journalEntryRepository.findById(journalEntryId)
                    .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", journalEntryId));
            accountingPostingService.reverseJournal(original, actor, reason, LocalDate.now());
        }
        transfer.setStatus(TransactionStatus.CANCELLED);
        transfer = transferRepository.save(transfer);
        activityLogService.log(MODULE, "CANCEL", "Transfer", transfer.getId(), transfer.getReference(),
                "Cancelled transfer " + transfer.getReference());
        return toDisplay(transfer);
    }

    @Transactional
    public void delete(Long id) {
        Transfer transfer = loadTransfer(id);
        if (transfer.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft transfers can be deleted");
        }
        transferRepository.delete(transfer);
        activityLogService.log(MODULE, "DELETE", "Transfer", id, transfer.getReference(),
                "Deleted transfer " + transfer.getReference());
    }

    private void applyForm(Transfer transfer, TransferFormDto request) {
        if (request.getSourceAccountId().equals(request.getDestinationAccountId())) {
            throw new BusinessException("Source and destination accounts must be different");
        }
        loadActiveAccount(request.getSourceAccountId());
        loadActiveAccount(request.getDestinationAccountId());

        transfer.setTransferDate(request.getTransferDate());
        transfer.setDescription(normalizeOptional(request.getDescription()));
        transfer.setAmount(request.getAmount());
        transfer.setSourceAccountId(request.getSourceAccountId());
        transfer.setDestinationAccountId(request.getDestinationAccountId());
    }

    private Transfer loadTransfer(Long id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", id));
    }

    private String resolveReference(String requestedReference) {
        String normalized = normalizeOptional(requestedReference);
        if (normalized != null) {
            if (transferRepository.existsByReferenceIgnoreCase(normalized)) {
                throw new BusinessException("Transfer reference already exists");
            }
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("TRANSFER_REFERENCE");
        } catch (Exception exception) {
            return "TR-" + System.currentTimeMillis();
        }
    }

    private Account loadActiveAccount(Long accountId) {
        Account account = loadAccount(accountId);
        if (!account.isActive()) {
            throw new BusinessException("Account must be active");
        }
        return account;
    }

    private Account loadAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private TransferDisplayDto toDisplay(Transfer transfer) {
        Account source = loadAccount(transfer.getSourceAccountId());
        Account destination = loadAccount(transfer.getDestinationAccountId());
        return TransferDisplayDto.builder()
                .id(transfer.getId())
                .transferDate(transfer.getTransferDate())
                .reference(transfer.getReference())
                .description(transfer.getDescription())
                .amount(transfer.getAmount())
                .sourceAccountId(source.getId())
                .sourceAccountCode(source.getCode())
                .sourceAccountName(source.getNameEn())
                .destinationAccountId(destination.getId())
                .destinationAccountCode(destination.getCode())
                .destinationAccountName(destination.getNameEn())
                .status(transfer.getStatus())
                .postedAt(transfer.getPostedAt())
                .postedBy(transfer.getPostedBy())
                .journalEntryId(transfer.getJournalEntryId())
                .createdAt(transfer.getCreatedAt())
                .updatedAt(transfer.getUpdatedAt())
                .build();
    }
}
