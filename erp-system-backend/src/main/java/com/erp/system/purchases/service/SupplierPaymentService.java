package com.erp.system.purchases.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.JournalEntryRepository;
import com.erp.system.accounting.service.AccountingPostingService;
import com.erp.system.accounting.support.JournalPostingNarratives;
import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.service.NumberingService;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.purchases.domain.PurchaseInvoice;
import com.erp.system.purchases.domain.SupplierPayment;
import com.erp.system.purchases.dto.display.SupplierPaymentDisplayDto;
import com.erp.system.purchases.dto.form.SupplierPaymentFormDto;
import com.erp.system.purchases.repository.PurchaseInvoiceRepository;
import com.erp.system.purchases.repository.SupplierPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierPaymentService {

    private static final String MODULE = "PURCHASES";

    private final SupplierPaymentRepository supplierPaymentRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final NumberingService numberingService;
    private final ActivityLogService activityLogService;
    private final AccountRepository accountRepository;
    private final AccountingPostingService accountingPostingService;
    private final JournalEntryRepository journalEntryRepository;

    @Transactional(readOnly = true)
    public List<SupplierPaymentDisplayDto> getAll() {
        return supplierPaymentRepository.findAllByOrderByIdDesc().stream()
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplierPaymentDisplayDto getById(Long id) {
        return toDisplay(loadSupplierPayment(id));
    }

    @Transactional
    public SupplierPaymentDisplayDto create(SupplierPaymentFormDto request) {
        SupplierPayment payment = new SupplierPayment();
        applyForm(payment, request);
        payment.setPaymentNumber(resolveNumber(request.getPaymentNumber()));
        payment.setStatus(TransactionStatus.DRAFT);
        payment = supplierPaymentRepository.save(payment);
        activityLogService.log(MODULE, "CREATE", "SupplierPayment", payment.getId(), payment.getPaymentNumber(),
                "Created supplier payment " + payment.getPaymentNumber());
        return toDisplay(payment);
    }

    @Transactional
    public SupplierPaymentDisplayDto update(Long id, SupplierPaymentFormDto request) {
        SupplierPayment payment = loadSupplierPayment(id);
        if (payment.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft payments can be edited");
        }
        applyForm(payment, request);
        payment = supplierPaymentRepository.save(payment);
        activityLogService.log(MODULE, "UPDATE", "SupplierPayment", payment.getId(), payment.getPaymentNumber(),
                "Updated supplier payment " + payment.getPaymentNumber());
        return toDisplay(payment);
    }

    @Transactional
    public SupplierPaymentDisplayDto approve(Long id, String actor) {
        SupplierPayment payment = loadSupplierPayment(id);
        if (payment.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Cancelled payment cannot be approved");
        }
        if (payment.getStatus() == TransactionStatus.APPROVED) {
            return toDisplay(payment);
        }

        Account apAccount = accountRepository.findByCode("2110")
                .orElseThrow(() -> new BusinessException("Accounts payable account 2110 not found"));
        Account cashAccount = resolveCashBankAccount();
        String narrative = JournalPostingNarratives.entryHeader(
                payment.getNotes(),
                JournalPostingNarratives.PAYMENT_BOND,
                payment.getPaymentNumber()
        );

        JournalEntry journalEntry = accountingPostingService.createPostedJournal(
                payment.getPaymentDate(),
                narrative,
                "SUPPLIER_PAYMENT",
                payment.getId(),
                actor,
                List.of(
                        AccountingPostingService.JournalLineDraft.builder()
                                .accountId(apAccount.getId())
                                .description(JournalPostingNarratives.lineWithAccount(narrative, apAccount, true))
                                .debit(payment.getAmount())
                                .credit(BigDecimal.ZERO)
                                .build(),
                        AccountingPostingService.JournalLineDraft.builder()
                                .accountId(cashAccount.getId())
                                .description(JournalPostingNarratives.lineWithAccount(narrative, cashAccount, false))
                                .debit(BigDecimal.ZERO)
                                .credit(payment.getAmount())
                                .build()
                )
        );

        payment.setStatus(TransactionStatus.APPROVED);
        payment.setJournalEntryId(journalEntry.getId());
        payment = supplierPaymentRepository.save(payment);

        if (payment.getInvoiceId() != null) {
            final Long invoiceId = payment.getInvoiceId();
            PurchaseInvoice invoice = purchaseInvoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> new ResourceNotFoundException("PurchaseInvoice", invoiceId));
            invoice.setPaidAmount(invoice.getPaidAmount().add(payment.getAmount()));
            invoice.setRemainingAmount(invoice.getTotalAmount().subtract(invoice.getPaidAmount()).max(BigDecimal.ZERO));
            purchaseInvoiceRepository.save(invoice);
        }

        activityLogService.log(MODULE, "APPROVE", "SupplierPayment", payment.getId(), payment.getPaymentNumber(),
                "Approved supplier payment " + payment.getPaymentNumber());
        return toDisplay(payment);
    }

    @Transactional
    public SupplierPaymentDisplayDto cancel(Long id, String actor, String reason) {
        SupplierPayment payment = loadSupplierPayment(id);
        if (payment.getStatus() == TransactionStatus.CANCELLED) {
            return toDisplay(payment);
        }
        if (payment.getJournalEntryId() != null) {
            final Long journalEntryId = payment.getJournalEntryId();
            JournalEntry original = journalEntryRepository.findById(journalEntryId)
                    .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", journalEntryId));
            accountingPostingService.reverseJournal(original, actor, reason, LocalDate.now());
        }
        payment.setStatus(TransactionStatus.CANCELLED);
        payment = supplierPaymentRepository.save(payment);
        activityLogService.log(MODULE, "CANCEL", "SupplierPayment", payment.getId(), payment.getPaymentNumber(),
                "Cancelled supplier payment " + payment.getPaymentNumber());
        return toDisplay(payment);
    }

    @Transactional
    public void delete(Long id) {
        SupplierPayment payment = loadSupplierPayment(id);
        if (payment.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft payments can be deleted");
        }
        supplierPaymentRepository.delete(payment);
        activityLogService.log(MODULE, "DELETE", "SupplierPayment", id, payment.getPaymentNumber(),
                "Deleted supplier payment " + payment.getPaymentNumber());
    }

    private Account resolveCashBankAccount() {
        for (String code : List.of("1010", "1020", "1110")) {
            var account = accountRepository.findByCode(code);
            if (account.isPresent()) {
                return account.get();
            }
        }
        throw new BusinessException("Cash/bank account not found (tried 1010, 1020, 1110)");
    }

    private String resolveNumber(String requested) {
        String normalized = requested == null ? null : requested.trim();
        if (normalized != null && !normalized.isEmpty()) {
            if (supplierPaymentRepository.existsByPaymentNumberIgnoreCase(normalized)) {
                throw new BusinessException("Payment number already exists");
            }
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("SUPPLIER_PAYMENT");
        } catch (Exception exception) {
            return "SP-" + System.currentTimeMillis();
        }
    }

    private void applyForm(SupplierPayment payment, SupplierPaymentFormDto request) {
        payment.setPaymentDate(request.getPaymentDate());
        payment.setSupplierId(request.getSupplierId());
        payment.setInvoiceId(request.getInvoiceId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod().trim());
        payment.setNotes(request.getNotes());
    }

    private SupplierPayment loadSupplierPayment(Long id) {
        return supplierPaymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierPayment", id));
    }

    private SupplierPaymentDisplayDto toDisplay(SupplierPayment payment) {
        return SupplierPaymentDisplayDto.builder()
                .id(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .paymentDate(payment.getPaymentDate())
                .supplierId(payment.getSupplierId())
                .invoiceId(payment.getInvoiceId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .notes(payment.getNotes())
                .journalEntryId(payment.getJournalEntryId())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
