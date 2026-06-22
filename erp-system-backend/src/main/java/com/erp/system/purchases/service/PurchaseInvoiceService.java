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
import com.erp.system.inventory.service.StockService;
import com.erp.system.purchases.domain.PurchaseInvoice;
import com.erp.system.purchases.domain.PurchaseInvoiceLine;
import com.erp.system.purchases.dto.display.PurchaseInvoiceDisplayDto;
import com.erp.system.purchases.dto.display.PurchaseInvoiceLineDisplayDto;
import com.erp.system.purchases.dto.form.PurchaseInvoiceFormDto;
import com.erp.system.purchases.repository.PurchaseInvoiceLineRepository;
import com.erp.system.purchases.repository.PurchaseInvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseInvoiceService {

    private static final String MODULE = "PURCHASES";

    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final PurchaseInvoiceLineRepository purchaseInvoiceLineRepository;
    private final NumberingService numberingService;
    private final ActivityLogService activityLogService;
    private final StockService stockService;
    private final AccountingPostingService accountingPostingService;
    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;

    @Transactional(readOnly = true)
    public List<PurchaseInvoiceDisplayDto> getAll() {
        return purchaseInvoiceRepository.findAllByOrderByInvoiceDateDescIdDesc().stream()
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseInvoiceDisplayDto getById(Long id) {
        return toDisplay(loadPurchaseInvoice(id));
    }

    @Transactional
    public PurchaseInvoiceDisplayDto create(PurchaseInvoiceFormDto request) {
        PurchaseInvoice invoice = new PurchaseInvoice();
        applyForm(invoice, request);
        invoice.setInvoiceNumber(resolveNumber(request.getInvoiceNumber()));
        invoice.setStatus(TransactionStatus.DRAFT);
        invoice.setRemainingAmount(invoice.getTotalAmount());
        invoice = purchaseInvoiceRepository.save(invoice);
        activityLogService.log(MODULE, "CREATE", "PurchaseInvoice", invoice.getId(), invoice.getInvoiceNumber(),
                "Created purchase invoice " + invoice.getInvoiceNumber());
        return toDisplay(invoice);
    }

    @Transactional
    public PurchaseInvoiceDisplayDto update(Long id, PurchaseInvoiceFormDto request) {
        PurchaseInvoice invoice = loadPurchaseInvoice(id);
        if (invoice.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft invoices can be edited");
        }
        applyForm(invoice, request);
        if (request.getInvoiceNumber() != null && !request.getInvoiceNumber().isBlank()) {
            invoice.setInvoiceNumber(request.getInvoiceNumber().trim());
        }
        invoice.setRemainingAmount(invoice.getTotalAmount().subtract(invoice.getPaidAmount()).max(BigDecimal.ZERO));
        invoice = purchaseInvoiceRepository.save(invoice);
        activityLogService.log(MODULE, "UPDATE", "PurchaseInvoice", invoice.getId(), invoice.getInvoiceNumber(),
                "Updated purchase invoice " + invoice.getInvoiceNumber());
        return toDisplay(invoice);
    }

    @Transactional
    public PurchaseInvoiceDisplayDto approve(Long id, String actor) {
        PurchaseInvoice invoice = loadPurchaseInvoice(id);
        if (invoice.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Cancelled invoice cannot be approved");
        }
        if (invoice.getStatus() == TransactionStatus.APPROVED) {
            return toDisplay(invoice);
        }
        if (invoice.getWarehouseId() == null) {
            throw new BusinessException("Warehouse is required to approve purchase invoice");
        }

        List<PurchaseInvoiceLine> lines = purchaseInvoiceLineRepository.findByInvoiceIdOrderByIdAsc(invoice.getId());
        if (lines.isEmpty()) {
            throw new BusinessException("Cannot approve invoice without lines");
        }

        for (PurchaseInvoiceLine line : lines) {
            stockService.receiveStock(
                    line.getProductId(),
                    invoice.getWarehouseId(),
                    line.getQuantity(),
                    line.getUnitPrice(),
                    "PURCHASE_INVOICE",
                    invoice.getId(),
                    invoice.getInvoiceDate()
            );
        }

        Account inventoryAccount = accountRepository.findByCode("1300")
                .orElseThrow(() -> new BusinessException("Inventory account 1300 not found"));
        Account taxAccount = accountRepository.findByCode("2210")
                .orElseThrow(() -> new BusinessException("Tax account 2210 not found"));
        Account payableAccount = accountRepository.findByCode("2110")
                .orElseThrow(() -> new BusinessException("Payables account 2110 not found"));

        BigDecimal taxAmount = invoice.getTaxAmount() == null ? BigDecimal.ZERO : invoice.getTaxAmount();
        BigDecimal inventoryAmount = invoice.getTotalAmount().subtract(taxAmount).max(BigDecimal.ZERO);
        String narrative = JournalPostingNarratives.entryHeader(
                invoice.getNotes(),
                JournalPostingNarratives.PURCHASE_INVOICE,
                invoice.getInvoiceNumber()
        );

        List<AccountingPostingService.JournalLineDraft> journalLines = new ArrayList<>();
        journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                .accountId(inventoryAccount.getId())
                .description(JournalPostingNarratives.lineWithAccount(narrative, inventoryAccount, true))
                .debit(inventoryAmount)
                .credit(BigDecimal.ZERO)
                .build());
        if (taxAmount.compareTo(BigDecimal.ZERO) > 0) {
            journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                    .accountId(taxAccount.getId())
                    .description(JournalPostingNarratives.lineWithAccount(narrative, taxAccount, true))
                    .debit(taxAmount)
                    .credit(BigDecimal.ZERO)
                    .build());
        }
        journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                .accountId(payableAccount.getId())
                .description(JournalPostingNarratives.lineWithAccount(narrative, payableAccount, false))
                .debit(BigDecimal.ZERO)
                .credit(invoice.getTotalAmount())
                .build());

        JournalEntry journalEntry = accountingPostingService.createPostedJournal(
                invoice.getInvoiceDate(),
                narrative,
                "PURCHASE_INVOICE",
                invoice.getId(),
                actor,
                journalLines
        );

        invoice.setJournalEntryId(journalEntry.getId());
        invoice.setStatus(TransactionStatus.APPROVED);
        invoice.setApprovedAt(LocalDateTime.now());
        invoice.setApprovedBy(actor);
        invoice = purchaseInvoiceRepository.save(invoice);

        activityLogService.log(MODULE, "APPROVE", "PurchaseInvoice", invoice.getId(), invoice.getInvoiceNumber(),
                "Approved purchase invoice " + invoice.getInvoiceNumber());
        return toDisplay(invoice);
    }

    @Transactional
    public PurchaseInvoiceDisplayDto cancel(Long id, String actor, String reason) {
        PurchaseInvoice invoice = loadPurchaseInvoice(id);
        if (invoice.getStatus() == TransactionStatus.CANCELLED) {
            return toDisplay(invoice);
        }
        if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Cannot cancel invoice with posted payments");
        }

        if (invoice.getJournalEntryId() != null) {
            final Long journalEntryId = invoice.getJournalEntryId();
            JournalEntry original = journalEntryRepository.findById(journalEntryId)
                    .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", journalEntryId));
            JournalEntry reversal = accountingPostingService.reverseJournal(
                    original,
                    actor,
                    reason,
                    LocalDate.now()
            );
            invoice.setCancellationJournalEntryId(reversal.getId());
        }
        invoice.setStatus(TransactionStatus.CANCELLED);
        invoice.setCancelledAt(LocalDateTime.now());
        invoice.setCancelledBy(actor);
        invoice = purchaseInvoiceRepository.save(invoice);
        activityLogService.log(MODULE, "CANCEL", "PurchaseInvoice", invoice.getId(), invoice.getInvoiceNumber(),
                "Cancelled purchase invoice " + invoice.getInvoiceNumber());
        return toDisplay(invoice);
    }

    @Transactional
    public void delete(Long id) {
        PurchaseInvoice invoice = loadPurchaseInvoice(id);
        if (invoice.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft invoices can be deleted");
        }
        purchaseInvoiceRepository.delete(invoice);
        activityLogService.log(MODULE, "DELETE", "PurchaseInvoice", id, invoice.getInvoiceNumber(),
                "Deleted purchase invoice " + invoice.getInvoiceNumber());
    }

    private String resolveNumber(String requested) {
        String normalized = requested == null ? null : requested.trim();
        if (normalized != null && !normalized.isEmpty()) {
            if (purchaseInvoiceRepository.existsByInvoiceNumberIgnoreCase(normalized)) {
                throw new BusinessException("Invoice number already exists");
            }
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("PURCHASE_INVOICE");
        } catch (Exception exception) {
            return "PINV-" + System.currentTimeMillis();
        }
    }

    private void applyForm(PurchaseInvoice invoice, PurchaseInvoiceFormDto request) {
        if (request.getDueDate().isBefore(request.getInvoiceDate())) {
            throw new BusinessException("Due date cannot be before invoice date");
        }
        invoice.setInvoiceDate(request.getInvoiceDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setSupplierId(request.getSupplierId());
        invoice.setOrderId(request.getOrderId());
        invoice.setWarehouseId(request.getWarehouseId());
        invoice.setSubtotal(request.getSubtotal());
        invoice.setDiscountAmount(request.getDiscountAmount() == null ? BigDecimal.ZERO : request.getDiscountAmount());
        invoice.setTaxAmount(request.getTaxAmount() == null ? BigDecimal.ZERO : request.getTaxAmount());
        invoice.setTotalAmount(request.getTotalAmount());
        invoice.setNotes(request.getNotes());
    }

    private PurchaseInvoice loadPurchaseInvoice(Long id) {
        return purchaseInvoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseInvoice", id));
    }

    private PurchaseInvoiceDisplayDto toDisplay(PurchaseInvoice invoice) {
        List<PurchaseInvoiceLineDisplayDto> lines = purchaseInvoiceLineRepository.findByInvoiceIdOrderByIdAsc(invoice.getId())
                .stream()
                .map(line -> PurchaseInvoiceLineDisplayDto.builder()
                        .id(line.getId())
                        .invoiceId(line.getInvoiceId())
                        .productId(line.getProductId())
                        .description(line.getDescription())
                        .quantity(line.getQuantity())
                        .unitPrice(line.getUnitPrice())
                        .discountPercent(line.getDiscountPercent())
                        .taxPercent(line.getTaxPercent())
                        .lineTotal(line.getLineTotal())
                        .createdAt(line.getCreatedAt())
                        .updatedAt(line.getUpdatedAt())
                        .build())
                .toList();
        return PurchaseInvoiceDisplayDto.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceDate(invoice.getInvoiceDate())
                .dueDate(invoice.getDueDate())
                .supplierId(invoice.getSupplierId())
                .orderId(invoice.getOrderId())
                .warehouseId(invoice.getWarehouseId())
                .status(invoice.getStatus())
                .subtotal(invoice.getSubtotal())
                .discountAmount(invoice.getDiscountAmount())
                .taxAmount(invoice.getTaxAmount())
                .totalAmount(invoice.getTotalAmount())
                .paidAmount(invoice.getPaidAmount())
                .remainingAmount(invoice.getRemainingAmount())
                .notes(invoice.getNotes())
                .journalEntryId(invoice.getJournalEntryId())
                .cancellationJournalEntryId(invoice.getCancellationJournalEntryId())
                .approvedAt(invoice.getApprovedAt())
                .approvedBy(invoice.getApprovedBy())
                .cancelledAt(invoice.getCancelledAt())
                .cancelledBy(invoice.getCancelledBy())
                .lines(lines)
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }
}
