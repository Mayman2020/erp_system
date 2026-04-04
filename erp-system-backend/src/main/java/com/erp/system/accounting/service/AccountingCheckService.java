package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.AccountingCheck;
import com.erp.system.accounting.domain.BankAccount;
import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.accounting.dto.display.AccountingCheckDisplayDto;
import com.erp.system.accounting.dto.form.AccountingCheckFormDto;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.AccountingCheckRepository;
import com.erp.system.accounting.repository.BankAccountRepository;
import com.erp.system.common.enums.AccountingType;
import com.erp.system.common.enums.CheckStatus;
import com.erp.system.common.enums.CheckType;
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
public class AccountingCheckService {

    private final AccountingCheckRepository checkRepository;
    private final BankAccountRepository bankAccountRepository;
    private final AccountRepository accountRepository;
    private final NumberingService numberingService;
    private final AccountingPostingService accountingPostingService;

    @Transactional(readOnly = true)
    public List<AccountingCheckDisplayDto> getChecks(CheckType type, CheckStatus status, String search) {
        List<AccountingCheck> checks = status != null
                ? checkRepository.findByStatusOrderByIssueDateDescIdDesc(status)
                : type != null
                ? checkRepository.findByCheckTypeOrderByIssueDateDescIdDesc(type)
                : checkRepository.findAllByOrderByIssueDateDescIdDesc();

        String normalizedSearch = search == null || search.isBlank() ? null : search.trim().toLowerCase();
        return checks.stream()
                .filter(check -> type == null || check.getCheckType() == type)
                .filter(check -> normalizedSearch == null
                        || check.getCheckNumber().toLowerCase().contains(normalizedSearch)
                        || check.getBankName().toLowerCase().contains(normalizedSearch)
                        || (check.getPartyName() != null && check.getPartyName().toLowerCase().contains(normalizedSearch)))
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountingCheckDisplayDto getCheck(Long id) {
        return toDisplay(loadCheck(id));
    }

    @Transactional
    public AccountingCheckDisplayDto createCheck(AccountingCheckFormDto request) {
        AccountingCheck check = AccountingCheck.builder()
                .checkNumber(resolveCheckNumber(request.getCheckNumber()))
                .status(CheckStatus.PENDING)
                .build();
        applyForm(check, request);
        return toDisplay(checkRepository.save(check));
    }

    @Transactional
    public AccountingCheckDisplayDto updateCheck(Long id, AccountingCheckFormDto request) {
        AccountingCheck check = loadCheck(id);
        if (check.getStatus() != CheckStatus.PENDING) {
            throw new BusinessException("Only pending checks can be edited");
        }
        applyForm(check, request);
        return toDisplay(checkRepository.save(check));
    }

    @Transactional
    public AccountingCheckDisplayDto depositCheck(Long id, String actor) {
        AccountingCheck check = loadCheck(id);
        if (check.getCheckType() != CheckType.RECEIVED || check.getStatus() != CheckStatus.PENDING) {
            throw new BusinessException("Only pending received checks can be deposited");
        }
        JournalEntry journalEntry = accountingPostingService.createPostedJournal(
                check.getDueDate(),
                "Check deposit " + check.getCheckNumber(),
                "CHECK",
                check.getId(),
                actor,
                List.of(
                        AccountingPostingService.JournalLineDraft.builder()
                                .accountId(check.getBankAccount().getLinkedAccount().getId())
                                .description("Received check deposit")
                                .debit(check.getAmount())
                                .credit(BigDecimal.ZERO)
                                .build(),
                        AccountingPostingService.JournalLineDraft.builder()
                                .accountId(check.getHoldingAccount().getId())
                                .description("Remove received check from holding")
                                .debit(BigDecimal.ZERO)
                                .credit(check.getAmount())
                                .build()
                )
        );
        check.setJournalEntry(journalEntry);
        check.setStatus(CheckStatus.DEPOSITED);
        return toDisplay(checkRepository.save(check));
    }

    @Transactional
    public AccountingCheckDisplayDto clearCheck(Long id, String actor) {
        AccountingCheck check = loadCheck(id);
        if (check.getCheckType() == CheckType.RECEIVED) {
            if (check.getStatus() != CheckStatus.DEPOSITED) {
                throw new BusinessException("Received checks can only be cleared after deposit");
            }
            check.setStatus(CheckStatus.CLEARED);
            check.setClearedAt(LocalDateTime.now());
            return toDisplay(checkRepository.save(check));
        }

        if (check.getStatus() != CheckStatus.PENDING) {
            throw new BusinessException("Issued checks can only be cleared from pending status");
        }

        JournalEntry journalEntry = accountingPostingService.createPostedJournal(
                check.getDueDate(),
                "Issued check clearance " + check.getCheckNumber(),
                "CHECK",
                check.getId(),
                actor,
                List.of(
                        AccountingPostingService.JournalLineDraft.builder()
                                .accountId(check.getHoldingAccount().getId())
                                .description("Clear issued check holding")
                                .debit(check.getAmount())
                                .credit(BigDecimal.ZERO)
                                .build(),
                        AccountingPostingService.JournalLineDraft.builder()
                                .accountId(check.getBankAccount().getLinkedAccount().getId())
                                .description("Reduce bank for issued check")
                                .debit(BigDecimal.ZERO)
                                .credit(check.getAmount())
                                .build()
                )
        );
        check.setJournalEntry(journalEntry);
        check.setStatus(CheckStatus.CLEARED);
        check.setClearedAt(LocalDateTime.now());
        return toDisplay(checkRepository.save(check));
    }

    @Transactional
    public AccountingCheckDisplayDto bounceCheck(Long id, String actor, String reason) {
        AccountingCheck check = loadCheck(id);
        if (check.getStatus() != CheckStatus.DEPOSITED && check.getStatus() != CheckStatus.CLEARED) {
            throw new BusinessException("Only deposited or cleared checks can be bounced");
        }
        if (check.getJournalEntry() != null) {
            JournalEntry reversalEntry = accountingPostingService.reverseJournal(check.getJournalEntry(), actor, reason, LocalDate.now());
            check.setReversalJournalEntry(reversalEntry);
        }
        check.setStatus(CheckStatus.BOUNCED);
        check.setBouncedAt(LocalDateTime.now());
        return toDisplay(checkRepository.save(check));
    }

    @Transactional
    public AccountingCheckDisplayDto cancelCheck(Long id, String actor, String reason) {
        AccountingCheck check = loadCheck(id);
        if (check.getStatus() == CheckStatus.CANCELLED) {
            throw new BusinessException("Check is already cancelled");
        }
        if (check.getJournalEntry() != null && check.getReversalJournalEntry() == null) {
            JournalEntry reversalEntry = accountingPostingService.reverseJournal(check.getJournalEntry(), actor, reason, LocalDate.now());
            check.setReversalJournalEntry(reversalEntry);
        }
        check.setStatus(CheckStatus.CANCELLED);
        return toDisplay(checkRepository.save(check));
    }

    private void applyForm(AccountingCheck check, AccountingCheckFormDto request) {
        if (request.getDueDate().isBefore(request.getIssueDate())) {
            throw new BusinessException("Due date cannot be before issue date");
        }
        BankAccount bankAccount = bankAccountRepository.findById(request.getBankAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("BankAccount", request.getBankAccountId()));
        Account holdingAccount = accountRepository.findById(request.getHoldingAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", request.getHoldingAccountId()));
        if (!holdingAccount.isActive()) {
            throw new BusinessException("Holding account must be active");
        }
        if (holdingAccount.getAccountType() != AccountingType.ASSET && holdingAccount.getAccountType() != AccountingType.LIABILITY) {
            throw new BusinessException("Holding account must be an asset or liability account");
        }

        check.setCheckType(request.getCheckType());
        check.setIssueDate(request.getIssueDate());
        check.setDueDate(request.getDueDate());
        check.setBankName(request.getBankName().trim());
        check.setAmount(request.getAmount().setScale(2, RoundingMode.HALF_UP));
        check.setPartyName(request.getPartyName() == null ? null : request.getPartyName().trim());
        check.setLinkedDocumentReference(request.getLinkedDocumentReference() == null ? null : request.getLinkedDocumentReference().trim());
        check.setBankAccount(bankAccount);
        check.setHoldingAccount(holdingAccount);
    }

    private AccountingCheck loadCheck(Long id) {
        return checkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AccountingCheck", id));
    }

    private String resolveCheckNumber(String checkNumber) {
        if (checkNumber != null && !checkNumber.isBlank()) {
            String normalized = checkNumber.trim();
            if (checkRepository.existsByCheckNumberIgnoreCase(normalized)) {
                throw new BusinessException("Check number already exists");
            }
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("CHECK_NUMBER");
        } catch (Exception exception) {
            return "CHK-" + System.currentTimeMillis();
        }
    }

    private AccountingCheckDisplayDto toDisplay(AccountingCheck check) {
        return AccountingCheckDisplayDto.builder()
                .id(check.getId())
                .checkNumber(check.getCheckNumber())
                .checkType(check.getCheckType())
                .bankName(check.getBankName())
                .issueDate(check.getIssueDate())
                .dueDate(check.getDueDate())
                .amount(check.getAmount())
                .status(check.getStatus())
                .partyName(check.getPartyName())
                .linkedDocumentReference(check.getLinkedDocumentReference())
                .bankAccountId(check.getBankAccount() != null ? check.getBankAccount().getId() : null)
                .bankAccountNumber(check.getBankAccount() != null ? check.getBankAccount().getAccountNumber() : null)
                .holdingAccountId(check.getHoldingAccount() != null ? check.getHoldingAccount().getId() : null)
                .holdingAccountCode(check.getHoldingAccount() != null ? check.getHoldingAccount().getCode() : null)
                .holdingAccountName(check.getHoldingAccount() != null ? check.getHoldingAccount().getNameEn() : null)
                .journalEntryId(check.getJournalEntry() != null ? check.getJournalEntry().getId() : null)
                .reversalJournalEntryId(check.getReversalJournalEntry() != null ? check.getReversalJournalEntry().getId() : null)
                .clearedAt(check.getClearedAt())
                .bouncedAt(check.getBouncedAt())
                .createdAt(check.getCreatedAt())
                .updatedAt(check.getUpdatedAt())
                .build();
    }
}
