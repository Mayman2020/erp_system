package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.CustomerInvoice;
import com.erp.system.accounting.domain.CustomerInvoiceLine;
import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.accounting.dto.display.CustomerInvoiceDisplayDto;
import com.erp.system.accounting.dto.display.CustomerInvoiceLineDisplayDto;
import com.erp.system.accounting.dto.form.CustomerInvoiceFormDto;
import com.erp.system.accounting.dto.form.CustomerInvoiceLineFormDto;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.CustomerInvoiceRepository;
import com.erp.system.accounting.repository.ReceiptVoucherRepository;
import com.erp.system.common.enums.AccountingType;
import com.erp.system.common.enums.InvoiceStatus;
import com.erp.system.common.enums.VoucherStatus;
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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerInvoiceService {

    private final CustomerInvoiceRepository invoiceRepository;
    private final AccountRepository accountRepository;
    private final ReceiptVoucherRepository receiptVoucherRepository;
    private final NumberingService numberingService;
    private final AccountingPostingService accountingPostingService;

    @Transactional(readOnly = true)
    public List<CustomerInvoiceDisplayDto> getInvoices(InvoiceStatus status, String search) {
        List<CustomerInvoice> invoices = status == null
                ? invoiceRepository.findAllByOrderByInvoiceDateDescIdDesc()
                : invoiceRepository.findByStatusOrderByInvoiceDateDescIdDesc(status);

        String normalizedSearch = (search == null || search.isBlank()) ? null : search.trim().toLowerCase();
        return invoices.stream()
                .filter(invoice -> normalizedSearch == null
                        || invoice.getInvoiceNumber().toLowerCase().contains(normalizedSearch)
                        || (invoice.getCustomerName() != null && invoice.getCustomerName().toLowerCase().contains(normalizedSearch))
                        || (invoice.getDescription() != null && invoice.getDescription().toLowerCase().contains(normalizedSearch)))
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerInvoiceDisplayDto getInvoice(Long id) {
        return toDisplay(loadInvoice(id));
    }

    @Transactional
    public CustomerInvoiceDisplayDto createInvoice(CustomerInvoiceFormDto request) {
        CustomerInvoice invoice = CustomerInvoice.builder()
                .invoiceNumber(resolveInvoiceNumber(request.getInvoiceNumber()))
                .status(InvoiceStatus.DRAFT)
                .lines(new ArrayList<>())
                .build();
        applyForm(invoice, request);
        return toDisplay(invoiceRepository.save(invoice));
    }

    @Transactional
    public CustomerInvoiceDisplayDto updateInvoice(Long id, CustomerInvoiceFormDto request) {
        CustomerInvoice invoice = loadInvoice(id);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new BusinessException("Only draft invoices can be edited");
        }
        applyForm(invoice, request);
        return toDisplay(invoiceRepository.save(invoice));
    }

    @Transactional
    public CustomerInvoiceDisplayDto postInvoice(Long id, String actor) {
        CustomerInvoice invoice = loadInvoice(id);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new BusinessException("Only draft invoices can be posted");
        }

        List<AccountingPostingService.JournalLineDraft> journalLines = new ArrayList<>();
        journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                .accountId(invoice.getReceivableAccount().getId())
                .description("Customer receivable")
                .debit(invoice.getTotalAmount())
                .credit(BigDecimal.ZERO)
                .build());

        for (CustomerInvoiceLine line : invoice.getLines()) {
            journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                    .accountId(line.getAccount().getId())
                    .description(line.getDescription())
                    .debit(BigDecimal.ZERO)
                    .credit(line.getLineTotal())
                    .build());
        }
        if (invoice.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                    .accountId(invoice.getRevenueAccount().getId())
                    .description("Invoice tax allocation")
                    .debit(BigDecimal.ZERO)
                    .credit(invoice.getTaxAmount())
                    .build());
        }

        JournalEntry journalEntry = accountingPostingService.createPostedJournal(
                invoice.getInvoiceDate(),
                invoice.getDescription(),
                "CUSTOMER_INVOICE",
                invoice.getId(),
                actor,
                journalLines
        );

        invoice.setJournalEntry(journalEntry);
        invoice.setStatus(InvoiceStatus.POSTED);
        invoice.setPostedAt(LocalDateTime.now());
        invoice.setPostedBy(actor);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setOutstandingAmount(invoice.getTotalAmount());
        return toDisplay(invoiceRepository.save(invoice));
    }

    @Transactional
    public CustomerInvoiceDisplayDto cancelInvoice(Long id, String actor, String reason) {
        CustomerInvoice invoice = loadInvoice(id);
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BusinessException("Invoice is already cancelled");
        }
        if (invoice.getStatus() == InvoiceStatus.POSTED || invoice.getStatus() == InvoiceStatus.PARTIAL || invoice.getStatus() == InvoiceStatus.PAID) {
            JournalEntry reversalEntry = accountingPostingService.reverseJournal(invoice.getJournalEntry(), actor, reason, LocalDate.now());
            invoice.setCancellationJournalEntry(reversalEntry);
        }
        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice.setCancelledAt(LocalDateTime.now());
        invoice.setCancelledBy(actor);
        invoice.setOutstandingAmount(BigDecimal.ZERO);
        return toDisplay(invoiceRepository.save(invoice));
    }

    @Transactional
    public void refreshInvoicePaymentStatus(Long invoiceId) {
        CustomerInvoice invoice = loadInvoice(invoiceId);
        refreshInvoicePaymentStatus(invoice.getInvoiceNumber());
    }

    @Transactional
    public void refreshInvoicePaymentStatus(String invoiceNumber) {
        CustomerInvoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber);
        if (invoice == null) {
            return;
        }

        BigDecimal paidAmount = receiptVoucherRepository.findByInvoiceReferenceOrderByVoucherDateDescIdDesc(invoice.getInvoiceNumber()).stream()
                .filter(v -> v.getJournalEntry() != null && v.getStatus() != VoucherStatus.CANCELLED)
                .map(voucher -> voucher.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.setPaidAmount(paidAmount);
        invoice.setOutstandingAmount(invoice.getTotalAmount().subtract(paidAmount).max(BigDecimal.ZERO));
        if (invoice.getStatus() != InvoiceStatus.CANCELLED) {
            if (invoice.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0) {
                invoice.setStatus(InvoiceStatus.PAID);
            } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                invoice.setStatus(InvoiceStatus.PARTIAL);
            } else if (invoice.getJournalEntry() != null) {
                invoice.setStatus(InvoiceStatus.POSTED);
            }
        }
        invoiceRepository.save(invoice);
    }

    private void applyForm(CustomerInvoice invoice, CustomerInvoiceFormDto request) {
        if (request.getDueDate().isBefore(request.getInvoiceDate())) {
            throw new BusinessException("Due date cannot be before invoice date");
        }

        Account receivableAccount = resolveReceivableAccount(request.getReceivableAccountId());
        Account revenueAccount = resolveRevenueAccount(request.getRevenueAccountId());

        invoice.setInvoiceDate(request.getInvoiceDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setCustomerName(normalizeOptional(request.getCustomerName()));
        invoice.setCustomerReference(normalizeOptional(request.getCustomerReference()));
        invoice.setDescription(normalizeOptional(request.getDescription()));
        invoice.setReceivableAccount(receivableAccount);
        invoice.setRevenueAccount(revenueAccount);
        invoice.setTaxAmount(normalizeAmount(request.getTaxAmount()));

        invoice.getLines().clear();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CustomerInvoiceLineFormDto lineRequest : request.getLines()) {
            Account lineAccount = resolveRevenueAccount(lineRequest.getAccountId());
            BigDecimal quantity = normalizePositive(lineRequest.getQuantity(), "Quantity");
            BigDecimal unitPrice = normalizeAmount(lineRequest.getUnitPrice());
            BigDecimal lineTotal = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);

            CustomerInvoiceLine line = CustomerInvoiceLine.builder()
                    .invoice(invoice)
                    .account(lineAccount)
                    .description(normalizeOptional(lineRequest.getDescription()))
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .lineTotal(lineTotal)
                    .build();

            invoice.getLines().add(line);
            subtotal = subtotal.add(lineTotal);
        }

        invoice.setSubtotal(subtotal);
        invoice.setTotalAmount(subtotal.add(invoice.getTaxAmount()).setScale(2, RoundingMode.HALF_UP));
        invoice.setOutstandingAmount(invoice.getTotalAmount().subtract(invoice.getPaidAmount()).max(BigDecimal.ZERO));
    }

    private Account resolveReceivableAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));
        if (!account.isActive() || account.getAccountType() != AccountingType.ASSET) {
            throw new BusinessException("Receivable account must be an active asset account");
        }
        return account;
    }

    private Account resolveRevenueAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));
        if (!account.isActive() || account.getAccountType() != AccountingType.REVENUE) {
            throw new BusinessException("Revenue account must be an active revenue account");
        }
        return account;
    }

    private String resolveInvoiceNumber(String invoiceNumber) {
        String normalized = normalizeOptional(invoiceNumber);
        if (normalized != null) {
            if (invoiceRepository.existsByInvoiceNumberIgnoreCase(normalized)) {
                throw new BusinessException("Invoice number already exists");
            }
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("CUSTOMER_INVOICE");
        } catch (Exception exception) {
            return "INV-" + System.currentTimeMillis();
        }
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Amount cannot be negative");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizePositive(BigDecimal amount, String label) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(label + " must be greater than zero");
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

    private CustomerInvoice loadInvoice(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerInvoice", id));
    }

    private CustomerInvoiceDisplayDto toDisplay(CustomerInvoice invoice) {
        return CustomerInvoiceDisplayDto.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceDate(invoice.getInvoiceDate())
                .dueDate(invoice.getDueDate())
                .customerName(invoice.getCustomerName())
                .customerReference(invoice.getCustomerReference())
                .description(invoice.getDescription())
                .subtotal(invoice.getSubtotal())
                .taxAmount(invoice.getTaxAmount())
                .totalAmount(invoice.getTotalAmount())
                .paidAmount(invoice.getPaidAmount())
                .outstandingAmount(invoice.getOutstandingAmount())
                .status(invoice.getStatus())
                .receivableAccountId(invoice.getReceivableAccount() != null ? invoice.getReceivableAccount().getId() : null)
                .receivableAccountCode(invoice.getReceivableAccount() != null ? invoice.getReceivableAccount().getCode() : null)
                .receivableAccountName(invoice.getReceivableAccount() != null ? invoice.getReceivableAccount().getNameEn() : null)
                .revenueAccountId(invoice.getRevenueAccount() != null ? invoice.getRevenueAccount().getId() : null)
                .revenueAccountCode(invoice.getRevenueAccount() != null ? invoice.getRevenueAccount().getCode() : null)
                .revenueAccountName(invoice.getRevenueAccount() != null ? invoice.getRevenueAccount().getNameEn() : null)
                .journalEntryId(invoice.getJournalEntry() != null ? invoice.getJournalEntry().getId() : null)
                .cancellationJournalEntryId(invoice.getCancellationJournalEntry() != null ? invoice.getCancellationJournalEntry().getId() : null)
                .postedAt(invoice.getPostedAt())
                .postedBy(invoice.getPostedBy())
                .cancelledAt(invoice.getCancelledAt())
                .cancelledBy(invoice.getCancelledBy())
                .lines(invoice.getLines().stream().map(line -> CustomerInvoiceLineDisplayDto.builder()
                        .id(line.getId())
                        .accountId(line.getAccount().getId())
                        .accountCode(line.getAccount().getCode())
                        .accountName(line.getAccount().getNameEn())
                        .description(line.getDescription())
                        .quantity(line.getQuantity())
                        .unitPrice(line.getUnitPrice())
                        .lineTotal(line.getLineTotal())
                        .build()).toList())
                .build();
    }
}
