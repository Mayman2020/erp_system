package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.accounting.domain.Transfer;
import com.erp.system.accounting.dto.display.TransferDisplayDto;
import com.erp.system.accounting.dto.form.TransferFormDto;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.TransferRepository;
import com.erp.system.common.enums.AccountingType;
import com.erp.system.common.enums.TransferStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.service.NumberingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final NumberingService numberingService;
    private final AccountingPostingService accountingPostingService;

    @Transactional(readOnly = true)
    public List<TransferDisplayDto> getTransfers(TransferStatus status,
                                                 LocalDate fromDate,
                                                 LocalDate toDate,
                                                 String search) {
        List<Transfer> transfers = status == null
                ? transferRepository.findAllByOrderByTransferDateDescIdDesc()
                : transferRepository.findByStatusOrderByTransferDateDescIdDesc(status);

        String normalizedSearch = search == null || search.isBlank() ? null : search.trim().toLowerCase();
        return transfers.stream()
                .filter(transfer -> fromDate == null || !transfer.getTransferDate().isBefore(fromDate))
                .filter(transfer -> toDate == null || !transfer.getTransferDate().isAfter(toDate))
                .filter(transfer -> normalizedSearch == null
                        || transfer.getReference().toLowerCase().contains(normalizedSearch)
                        || (transfer.getDescription() != null && transfer.getDescription().toLowerCase().contains(normalizedSearch)))
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransferDisplayDto getTransfer(Long id) {
        return toDisplay(loadTransfer(id));
    }

    @Transactional
    public TransferDisplayDto createTransfer(TransferFormDto request) {
        Transfer transfer = Transfer.builder()
                .reference(resolveReference(request.getReference()))
                .status(TransferStatus.DRAFT)
                .build();
        applyForm(transfer, request);
        return toDisplay(transferRepository.save(transfer));
    }

    @Transactional
    public TransferDisplayDto updateTransfer(Long id, TransferFormDto request) {
        Transfer transfer = loadTransfer(id);
        if (transfer.getStatus() != TransferStatus.DRAFT) {
            throw new BusinessException("Only draft transfers can be edited");
        }
        applyForm(transfer, request);
        return toDisplay(transferRepository.save(transfer));
    }

    @Transactional
    public TransferDisplayDto postTransfer(Long id, String actor) {
        Transfer transfer = loadTransfer(id);
        if (transfer.getStatus() != TransferStatus.DRAFT) {
            throw new BusinessException("Only draft transfers can be posted");
        }

        JournalEntry journalEntry = accountingPostingService.createPostedJournal(
                transfer.getTransferDate(),
                transfer.getDescription(),
                "TRANSFER",
                transfer.getId(),
                actor,
                List.of(
                        AccountingPostingService.JournalLineDraft.builder()
                                .accountId(transfer.getDestinationAccount().getId())
                                .description("Transfer destination")
                                .debit(transfer.getAmount())
                                .credit(BigDecimal.ZERO)
                                .build(),
                        AccountingPostingService.JournalLineDraft.builder()
                                .accountId(transfer.getSourceAccount().getId())
                                .description("Transfer source")
                                .debit(BigDecimal.ZERO)
                                .credit(transfer.getAmount())
                                .build()
                )
        );

        transfer.setStatus(TransferStatus.POSTED);
        transfer.setPostedAt(LocalDateTime.now());
        transfer.setPostedBy(actor);
        transfer.setJournalEntry(journalEntry);
        return toDisplay(transferRepository.save(transfer));
    }

    @Transactional
    public TransferDisplayDto cancelTransfer(Long id, String actor, String reason) {
        Transfer transfer = loadTransfer(id);
        if (transfer.getStatus() == TransferStatus.CANCELLED) {
            throw new BusinessException("Transfer is already cancelled");
        }
        if (transfer.getStatus() == TransferStatus.POSTED) {
            JournalEntry reversalEntry = accountingPostingService.reverseJournal(transfer.getJournalEntry(), actor, reason, LocalDate.now());
            transfer.setReversalJournalEntry(reversalEntry);
        }
        transfer.setStatus(TransferStatus.CANCELLED);
        return toDisplay(transferRepository.save(transfer));
    }

    private void applyForm(Transfer transfer, TransferFormDto request) {
        Account sourceAccount = resolveTransferAccount(request.getSourceAccountId(), "Source account");
        Account destinationAccount = resolveTransferAccount(request.getDestinationAccountId(), "Destination account");
        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new BusinessException("Source and destination accounts must be different");
        }

        transfer.setTransferDate(request.getTransferDate());
        transfer.setDescription(normalizeOptional(request.getDescription()));
        transfer.setAmount(normalizeAmount(request.getAmount()));
        transfer.setSourceAccount(sourceAccount);
        transfer.setDestinationAccount(destinationAccount);
    }

    private Account resolveTransferAccount(Long accountId, String label) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));
        if (!account.isActive() || !account.isPostable()) {
            throw new BusinessException(label + " must be active and postable");
        }
        if (account.getAccountType() != AccountingType.ASSET && account.getAccountType() != AccountingType.LIABILITY) {
            throw new BusinessException(label + " must be a balance sheet account");
        }
        return account;
    }

    private String resolveReference(String reference) {
        String normalized = normalizeOptional(reference);
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

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be greater than zero");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private Transfer loadTransfer(Long id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", id));
    }

    private TransferDisplayDto toDisplay(Transfer transfer) {
        return TransferDisplayDto.builder()
                .id(transfer.getId())
                .transferDate(transfer.getTransferDate())
                .reference(transfer.getReference())
                .description(transfer.getDescription())
                .amount(transfer.getAmount())
                .status(transfer.getStatus())
                .sourceAccountId(transfer.getSourceAccount().getId())
                .sourceAccountCode(transfer.getSourceAccount().getCode())
                .sourceAccountName(transfer.getSourceAccount().getNameEn())
                .destinationAccountId(transfer.getDestinationAccount().getId())
                .destinationAccountCode(transfer.getDestinationAccount().getCode())
                .destinationAccountName(transfer.getDestinationAccount().getNameEn())
                .journalEntryId(transfer.getJournalEntry() != null ? transfer.getJournalEntry().getId() : null)
                .reversalJournalEntryId(transfer.getReversalJournalEntry() != null ? transfer.getReversalJournalEntry().getId() : null)
                .postedAt(transfer.getPostedAt())
                .postedBy(transfer.getPostedBy())
                .createdAt(transfer.getCreatedAt())
                .updatedAt(transfer.getUpdatedAt())
                .build();
    }
}
