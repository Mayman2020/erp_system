package com.erp.system.partners.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.service.AccountingPostingService;
import com.erp.system.accounting.support.JournalPostingNarratives;
import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.partners.domain.Partner;
import com.erp.system.partners.domain.PartnerTransaction;
import com.erp.system.partners.dto.display.PartnerTransactionDisplayDto;
import com.erp.system.partners.dto.form.PartnerTransactionFormDto;
import com.erp.system.partners.repository.PartnerTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PartnerTransactionService {

    private static final String MODULE = "PARTNERS";
    private static final Set<String> ALLOWED_TYPES = Set.of("CAPITAL", "DRAWING");

    private final PartnerTransactionRepository partnerTransactionRepository;
    private final PartnerService partnerService;
    private final AccountRepository accountRepository;
    private final ObjectProvider<AccountingPostingService> accountingPostingServiceProvider;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<PartnerTransactionDisplayDto> getAll(Long partnerId) {
        List<PartnerTransaction> rows = partnerId == null
                ? partnerTransactionRepository.findAllByOrderByTxnDateDescIdDesc()
                : partnerTransactionRepository.findByPartnerIdOrderByTxnDateDescIdDesc(partnerId);
        return rows.stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public PartnerTransactionDisplayDto getById(Long id) {
        return toDisplay(loadTransaction(id));
    }

    @Transactional
    public PartnerTransactionDisplayDto create(PartnerTransactionFormDto request) {
        Partner partner = partnerService.loadPartner(request.getPartnerId());
        validateTxnType(request.getTxnType());
        validateAmount(request.getAmount());

        PartnerTransaction txn = PartnerTransaction.builder()
                .partnerId(partner.getId())
                .txnType(request.getTxnType().trim().toUpperCase(Locale.ROOT))
                .amount(request.getAmount())
                .txnDate(request.getTxnDate())
                .notes(normalizeNotes(request.getNotes()))
                .status(TransactionStatus.DRAFT)
                .build();
        txn = partnerTransactionRepository.save(txn);
        activityLogService.log(MODULE, "CREATE", "PartnerTransaction", txn.getId(), String.valueOf(txn.getId()),
                "Created partner transaction " + txn.getId());
        return toDisplay(txn);
    }

    @Transactional
    public PartnerTransactionDisplayDto update(Long id, PartnerTransactionFormDto request) {
        PartnerTransaction txn = loadTransaction(id);
        if (txn.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft transactions can be edited");
        }
        partnerService.loadPartner(request.getPartnerId());
        validateTxnType(request.getTxnType());
        validateAmount(request.getAmount());

        txn.setPartnerId(request.getPartnerId());
        txn.setTxnType(request.getTxnType().trim().toUpperCase(Locale.ROOT));
        txn.setAmount(request.getAmount());
        txn.setTxnDate(request.getTxnDate());
        txn.setNotes(normalizeNotes(request.getNotes()));
        txn = partnerTransactionRepository.save(txn);
        activityLogService.log(MODULE, "UPDATE", "PartnerTransaction", txn.getId(), String.valueOf(txn.getId()),
                "Updated partner transaction " + txn.getId());
        return toDisplay(txn);
    }

    @Transactional
    public PartnerTransactionDisplayDto approve(Long id, String actor) {
        PartnerTransaction txn = loadTransaction(id);
        if (txn.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Cancelled transaction cannot be approved");
        }
        if (txn.getStatus() == TransactionStatus.APPROVED) {
            return toDisplay(txn);
        }

        AccountingPostingService postingService = accountingPostingServiceProvider.getIfAvailable();
        if (postingService != null) {
            Partner partner = partnerService.loadPartner(txn.getPartnerId());
            Account cashAccount = resolveCashAccount();
            String narrative = JournalPostingNarratives.entryHeader(
                    txn.getNotes(),
                    "Partner " + txn.getTxnType(),
                    "PTXN-" + txn.getId()
            );
            List<AccountingPostingService.JournalLineDraft> lines = buildPostingLines(txn, partner, cashAccount, narrative);
            JournalEntry journalEntry = postingService.createPostedJournal(
                    txn.getTxnDate(),
                    narrative,
                    "PARTNER_TRANSACTION",
                    txn.getId(),
                    actor,
                    lines
            );
            txn.setJournalEntryId(journalEntry.getId());
        }

        txn.setStatus(TransactionStatus.APPROVED);
        txn = partnerTransactionRepository.save(txn);
        activityLogService.log(MODULE, "APPROVE", "PartnerTransaction", txn.getId(), String.valueOf(txn.getId()),
                "Approved partner transaction " + txn.getId());
        return toDisplay(txn);
    }

    @Transactional
    public void delete(Long id) {
        PartnerTransaction txn = loadTransaction(id);
        if (txn.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft transactions can be deleted");
        }
        partnerTransactionRepository.delete(txn);
        activityLogService.log(MODULE, "DELETE", "PartnerTransaction", txn.getId(), String.valueOf(txn.getId()),
                "Deleted partner transaction " + txn.getId());
    }

    private List<AccountingPostingService.JournalLineDraft> buildPostingLines(
            PartnerTransaction txn, Partner partner, Account cashAccount, String narrative) {
        List<AccountingPostingService.JournalLineDraft> lines = new ArrayList<>();
        if ("CAPITAL".equals(txn.getTxnType())) {
            Account capital = requirePartnerAccount(partner.getCapitalAccountId(), "capital");
            lines.add(debitLine(capital, txn.getAmount(), narrative));
            lines.add(creditLine(cashAccount, txn.getAmount(), narrative));
        } else if ("DRAWING".equals(txn.getTxnType())) {
            Account drawing = requirePartnerAccount(partner.getDrawingAccountId(), "drawing");
            lines.add(debitLine(drawing, txn.getAmount(), narrative));
            lines.add(creditLine(cashAccount, txn.getAmount(), narrative));
        } else {
            throw new BusinessException("Unsupported transaction type: " + txn.getTxnType());
        }
        return lines;
    }

    private Account requirePartnerAccount(Long accountId, String label) {
        if (accountId == null) {
            throw new BusinessException("Partner " + label + " account is required for posting");
        }
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("Partner account not found: " + accountId));
    }

    private Account resolveCashAccount() {
        return accountRepository.findByCode("1110")
                .orElseGet(() -> accountRepository.findByCode("1100")
                        .orElseThrow(() -> new BusinessException("Cash account not found")));
    }

    private AccountingPostingService.JournalLineDraft debitLine(Account account, BigDecimal amount, String narrative) {
        return AccountingPostingService.JournalLineDraft.builder()
                .accountId(account.getId())
                .description(JournalPostingNarratives.lineWithAccount(narrative, account, true))
                .debit(amount)
                .credit(BigDecimal.ZERO)
                .build();
    }

    private AccountingPostingService.JournalLineDraft creditLine(Account account, BigDecimal amount, String narrative) {
        return AccountingPostingService.JournalLineDraft.builder()
                .accountId(account.getId())
                .description(JournalPostingNarratives.lineWithAccount(narrative, account, false))
                .debit(BigDecimal.ZERO)
                .credit(amount)
                .build();
    }

    private PartnerTransaction loadTransaction(Long id) {
        return partnerTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner transaction not found: " + id));
    }

    private void validateTxnType(String txnType) {
        if (txnType == null || !ALLOWED_TYPES.contains(txnType.trim().toUpperCase(Locale.ROOT))) {
            throw new BusinessException("Transaction type must be CAPITAL or DRAWING");
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be greater than zero");
        }
    }

    private String normalizeNotes(String notes) {
        return notes == null || notes.isBlank() ? null : notes.trim();
    }

    private PartnerTransactionDisplayDto toDisplay(PartnerTransaction txn) {
        Partner partner = partnerService.loadPartner(txn.getPartnerId());
        return PartnerTransactionDisplayDto.builder()
                .id(txn.getId())
                .partnerId(txn.getPartnerId())
                .partnerCode(partner.getCode())
                .partnerName(partner.getName())
                .txnType(txn.getTxnType())
                .amount(txn.getAmount())
                .txnDate(txn.getTxnDate())
                .notes(txn.getNotes())
                .status(txn.getStatus().name())
                .journalEntryId(txn.getJournalEntryId())
                .createdAt(txn.getCreatedAt())
                .updatedAt(txn.getUpdatedAt())
                .createdBy(txn.getCreatedBy())
                .updatedBy(txn.getUpdatedBy())
                .build();
    }
}
