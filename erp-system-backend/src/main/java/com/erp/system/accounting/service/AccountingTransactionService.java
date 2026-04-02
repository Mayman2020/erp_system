package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.AccountingTransaction;
import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.accounting.dto.display.AccountingTransactionDisplayDto;
import com.erp.system.accounting.dto.form.AccountingTransactionFormDto;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.AccountingTransactionRepository;
import com.erp.system.common.enums.AccountingType;
import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.common.enums.TransactionType;
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
public class AccountingTransactionService {

    private final AccountingTransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final NumberingService numberingService;
    private final AccountingPostingService accountingPostingService;

    @Transactional(readOnly = true)
    public List<AccountingTransactionDisplayDto> getTransactions(TransactionType type,
                                                                 TransactionStatus status,
                                                                 String search) {
        List<AccountingTransaction> transactions = status != null
                ? transactionRepository.findByStatusOrderByTransactionDateDescIdDesc(status)
                : type != null
                ? transactionRepository.findByTransactionTypeOrderByTransactionDateDescIdDesc(type)
                : transactionRepository.findAllByOrderByTransactionDateDescIdDesc();

        String normalizedSearch = search == null || search.isBlank() ? null : search.trim().toLowerCase();
        return transactions.stream()
                .filter(transaction -> type == null || transaction.getTransactionType() == type)
                .filter(transaction -> normalizedSearch == null
                        || transaction.getReference().toLowerCase().contains(normalizedSearch)
                        || (transaction.getDescription() != null && transaction.getDescription().toLowerCase().contains(normalizedSearch))
                        || (transaction.getRelatedDocumentReference() != null && transaction.getRelatedDocumentReference().toLowerCase().contains(normalizedSearch)))
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountingTransactionDisplayDto getTransaction(Long id) {
        return toDisplay(loadTransaction(id));
    }

    @Transactional
    public AccountingTransactionDisplayDto createTransaction(AccountingTransactionFormDto request) {
        AccountingTransaction transaction = AccountingTransaction.builder()
                .reference(resolveReference(request.getReference()))
                .status(TransactionStatus.DRAFT)
                .build();
        applyForm(transaction, request);
        return toDisplay(transactionRepository.save(transaction));
    }

    @Transactional
    public AccountingTransactionDisplayDto updateTransaction(Long id, AccountingTransactionFormDto request) {
        AccountingTransaction transaction = loadTransaction(id);
        if (transaction.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft transactions can be edited");
        }
        applyForm(transaction, request);
        return toDisplay(transactionRepository.save(transaction));
    }

    @Transactional
    public AccountingTransactionDisplayDto postTransaction(Long id, String actor) {
        AccountingTransaction transaction = loadTransaction(id);
        if (transaction.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft transactions can be posted");
        }

        Account debitAccount = resolveDebitAccount(transaction);
        Account creditAccount = resolveCreditAccount(transaction);

        JournalEntry journalEntry = accountingPostingService.createPostedJournal(
                transaction.getTransactionDate(),
                transaction.getDescription(),
                "TRANSACTION",
                transaction.getId(),
                actor,
                List.of(
                        AccountingPostingService.JournalLineDraft.builder()
                                .accountId(debitAccount.getId())
                                .description(transaction.getTransactionType() + " debit")
                                .debit(transaction.getAmount())
                                .credit(BigDecimal.ZERO)
                                .build(),
                        AccountingPostingService.JournalLineDraft.builder()
                                .accountId(creditAccount.getId())
                                .description(transaction.getTransactionType() + " credit")
                                .debit(BigDecimal.ZERO)
                                .credit(transaction.getAmount())
                                .build()
                )
        );

        transaction.setDebitAccount(debitAccount);
        transaction.setCreditAccount(creditAccount);
        transaction.setJournalEntry(journalEntry);
        transaction.setStatus(TransactionStatus.POSTED);
        transaction.setPostedAt(LocalDateTime.now());
        transaction.setPostedBy(actor);
        return toDisplay(transactionRepository.save(transaction));
    }

    @Transactional
    public AccountingTransactionDisplayDto cancelTransaction(Long id, String actor, String reason) {
        AccountingTransaction transaction = loadTransaction(id);
        if (transaction.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Transaction is already cancelled");
        }
        if (transaction.getStatus() == TransactionStatus.POSTED) {
            accountingPostingService.reverseJournal(transaction.getJournalEntry(), actor, reason, LocalDate.now());
        }
        transaction.setStatus(TransactionStatus.CANCELLED);
        return toDisplay(transactionRepository.save(transaction));
    }

    private void applyForm(AccountingTransaction transaction, AccountingTransactionFormDto request) {
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setDescription(normalizeOptional(request.getDescription()));
        transaction.setTransactionType(request.getTransactionType());
        transaction.setAmount(normalizeAmount(request.getAmount()));
        transaction.setRelatedDocumentReference(normalizeOptional(request.getRelatedDocumentReference()));
        transaction.setOriginalTransaction(request.getOriginalTransactionId() == null
                ? null
                : loadTransaction(request.getOriginalTransactionId()));

        if (request.getDebitAccountId() != null) {
            transaction.setDebitAccount(loadAccount(request.getDebitAccountId()));
        }
        if (request.getCreditAccountId() != null) {
            transaction.setCreditAccount(loadAccount(request.getCreditAccountId()));
        }
    }

    private Account resolveDebitAccount(AccountingTransaction transaction) {
        if (transaction.getTransactionType() == TransactionType.REFUND && transaction.getOriginalTransaction() != null) {
            Account originalCredit = transaction.getOriginalTransaction().getCreditAccount();
            if (originalCredit == null) {
                throw new BusinessException("Refund source transaction must be posted with a credit account");
            }
            return originalCredit;
        }
        Account account = transaction.getDebitAccount();
        if (account == null) {
            throw new BusinessException("Debit account is required");
        }
        if (transaction.getTransactionType() == TransactionType.SALE && account.getAccountType() != AccountingType.ASSET) {
            throw new BusinessException("Sale transactions must debit a receivable or cash asset account");
        }
        if (transaction.getTransactionType() == TransactionType.PURCHASE
                && account.getAccountType() != AccountingType.EXPENSE
                && account.getAccountType() != AccountingType.ASSET) {
            throw new BusinessException("Purchase transactions must debit an expense or inventory asset account");
        }
        return account;
    }

    private Account resolveCreditAccount(AccountingTransaction transaction) {
        if (transaction.getTransactionType() == TransactionType.REFUND && transaction.getOriginalTransaction() != null) {
            Account originalDebit = transaction.getOriginalTransaction().getDebitAccount();
            if (originalDebit == null) {
                throw new BusinessException("Refund source transaction must be posted with a debit account");
            }
            return originalDebit;
        }
        Account account = transaction.getCreditAccount();
        if (account == null) {
            throw new BusinessException("Credit account is required");
        }
        if (transaction.getTransactionType() == TransactionType.SALE && account.getAccountType() != AccountingType.INCOME) {
            throw new BusinessException("Sale transactions must credit a revenue account");
        }
        if (transaction.getTransactionType() == TransactionType.PURCHASE
                && account.getAccountType() != AccountingType.LIABILITY
                && account.getAccountType() != AccountingType.ASSET) {
            throw new BusinessException("Purchase transactions must credit a payable or cash asset account");
        }
        return account;
    }

    private AccountingTransaction loadTransaction(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AccountingTransaction", id));
    }

    private Account loadAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", id));
        if (!account.isActive() || !account.isPostable()) {
            throw new BusinessException("Transaction account must be active and postable");
        }
        return account;
    }

    private String resolveReference(String reference) {
        String normalized = normalizeOptional(reference);
        if (normalized != null) {
            if (transactionRepository.existsByReferenceIgnoreCase(normalized)) {
                throw new BusinessException("Transaction reference already exists");
            }
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("TRANSACTION_REFERENCE");
        } catch (Exception exception) {
            return "TXN-" + System.currentTimeMillis();
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

    private AccountingTransactionDisplayDto toDisplay(AccountingTransaction transaction) {
        return AccountingTransactionDisplayDto.builder()
                .id(transaction.getId())
                .transactionDate(transaction.getTransactionDate())
                .reference(transaction.getReference())
                .description(transaction.getDescription())
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())
                .amount(transaction.getAmount())
                .debitAccountId(transaction.getDebitAccount() != null ? transaction.getDebitAccount().getId() : null)
                .debitAccountCode(transaction.getDebitAccount() != null ? transaction.getDebitAccount().getCode() : null)
                .debitAccountName(transaction.getDebitAccount() != null ? transaction.getDebitAccount().getNameEn() : null)
                .creditAccountId(transaction.getCreditAccount() != null ? transaction.getCreditAccount().getId() : null)
                .creditAccountCode(transaction.getCreditAccount() != null ? transaction.getCreditAccount().getCode() : null)
                .creditAccountName(transaction.getCreditAccount() != null ? transaction.getCreditAccount().getNameEn() : null)
                .originalTransactionId(transaction.getOriginalTransaction() != null ? transaction.getOriginalTransaction().getId() : null)
                .relatedDocumentReference(transaction.getRelatedDocumentReference())
                .journalEntryId(transaction.getJournalEntry() != null ? transaction.getJournalEntry().getId() : null)
                .postedAt(transaction.getPostedAt())
                .postedBy(transaction.getPostedBy())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
