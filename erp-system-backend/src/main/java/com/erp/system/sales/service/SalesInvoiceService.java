package com.erp.system.sales.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.accounting.service.AccountingPostingService;
import com.erp.system.accounting.support.JournalPostingNarratives;
import com.erp.system.accounting.support.PostingAccountResolver;
import com.erp.system.common.enums.StockMovementType;
import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.service.NumberingService;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.inventory.domain.Product;
import com.erp.system.inventory.domain.Warehouse;
import com.erp.system.inventory.dto.form.StockMovementFormDto;
import com.erp.system.inventory.repository.ProductRepository;
import com.erp.system.inventory.repository.WarehouseRepository;
import com.erp.system.inventory.service.StockService;
import com.erp.system.sales.domain.Customer;
import com.erp.system.sales.domain.SalesInvoice;
import com.erp.system.sales.domain.SalesInvoiceLine;
import com.erp.system.sales.domain.SalesOrder;
import com.erp.system.sales.dto.display.SalesInvoiceDisplayDto;
import com.erp.system.sales.dto.display.SalesInvoiceLineDisplayDto;
import com.erp.system.sales.dto.form.SalesInvoiceFormDto;
import com.erp.system.sales.dto.form.SalesInvoiceLineFormDto;
import com.erp.system.sales.repository.SalesInvoiceRepository;
import com.erp.system.sales.support.SalesDocumentTotalsSupport;
import com.erp.system.sales.support.SalesDocumentTotalsSupport.DocumentAmounts;
import com.erp.system.sales.support.SalesDocumentTotalsSupport.LineAmounts;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
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
public class SalesInvoiceService {

    private static final String MODULE = "SALES";
    private static final String REFERENCE_TYPE = "SALES_INVOICE";

    private final SalesInvoiceRepository invoiceRepository;
    private final SalesOrderService orderService;
    private final CustomerService customerService;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockService stockService;
    private final NumberingService numberingService;
    private final AccountingPostingService accountingPostingService;
    private final ActivityLogService activityLogService;
    private final PostingAccountResolver postingAccountResolver;

    @Transactional(readOnly = true)
    public List<SalesInvoiceDisplayDto> getInvoices(TransactionStatus status, String search,
                                                    LocalDate fromDate, LocalDate toDate) {
        List<SalesInvoice> invoices = status == null
                ? invoiceRepository.findAllByOrderByInvoiceDateDescIdDesc()
                : invoiceRepository.findByStatusOrderByInvoiceDateDescIdDesc(status);

        String normalizedSearch = search == null || search.isBlank() ? null : search.trim().toLowerCase();
        return invoices.stream()
                .filter(invoice -> fromDate == null || !invoice.getInvoiceDate().isBefore(fromDate))
                .filter(invoice -> toDate == null || !invoice.getInvoiceDate().isAfter(toDate))
                .filter(invoice -> normalizedSearch == null
                        || invoice.getInvoiceNumber().toLowerCase().contains(normalizedSearch)
                        || invoice.getCustomer().getCode().toLowerCase().contains(normalizedSearch)
                        || invoice.getCustomer().getNameEn().toLowerCase().contains(normalizedSearch))
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public SalesInvoiceDisplayDto getInvoice(Long id) {
        return toDisplay(loadInvoice(id));
    }

    @Transactional
    public SalesInvoiceDisplayDto createInvoice(SalesInvoiceFormDto request) {
        SalesInvoice invoice = SalesInvoice.builder()
                .invoiceNumber(resolveInvoiceNumber(request.getInvoiceNumber()))
                .status(TransactionStatus.DRAFT)
                .paidAmount(BigDecimal.ZERO)
                .lines(new ArrayList<>())
                .build();
        applyForm(invoice, request);
        invoice = invoiceRepository.save(invoice);

        activityLogService.log(MODULE, "CREATE", "SalesInvoice", invoice.getId(), invoice.getInvoiceNumber(),
                "Created sales invoice " + invoice.getInvoiceNumber());
        return toDisplay(invoice);
    }

    @Transactional
    public SalesInvoiceDisplayDto updateInvoice(Long id, SalesInvoiceFormDto request) {
        SalesInvoice invoice = loadInvoice(id);
        if (invoice.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft invoices can be edited");
        }
        applyForm(invoice, request);
        invoice = invoiceRepository.save(invoice);

        activityLogService.log(MODULE, "UPDATE", "SalesInvoice", invoice.getId(), invoice.getInvoiceNumber(),
                "Updated sales invoice " + invoice.getInvoiceNumber());
        return toDisplay(invoice);
    }

    @Transactional
    public void deleteInvoice(Long id) {
        SalesInvoice invoice = loadInvoice(id);
        if (invoice.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft invoices can be deleted");
        }
        invoiceRepository.delete(invoice);
        activityLogService.log(MODULE, "DELETE", "SalesInvoice", invoice.getId(), invoice.getInvoiceNumber(),
                "Deleted sales invoice " + invoice.getInvoiceNumber());
    }

    @Transactional
    public SalesInvoiceDisplayDto approveInvoice(Long id, String actor) {
        SalesInvoice invoice = loadInvoice(id);
        if (invoice.getStatus() == TransactionStatus.APPROVED) {
            return toDisplay(invoice);
        }
        if (invoice.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Cancelled invoices cannot be approved");
        }
        if (invoice.getLines().isEmpty()) {
            throw new BusinessException("Invoice must have at least one line");
        }
        if (invoice.getWarehouse() == null) {
            throw new BusinessException("Warehouse is required to approve a sales invoice");
        }

        Warehouse warehouse = invoice.getWarehouse();
        BigDecimal totalCogs = BigDecimal.ZERO;

        for (SalesInvoiceLine line : invoice.getLines()) {
            Product product = line.getProduct();
            BigDecimal lineCogs = product.getCostPrice().multiply(line.getQuantity()).setScale(2, RoundingMode.HALF_UP);
            totalCogs = totalCogs.add(lineCogs);

            StockMovementFormDto stockOut = new StockMovementFormDto();
            stockOut.setMovementDate(invoice.getInvoiceDate());
            stockOut.setMovementType(StockMovementType.OUT);
            stockOut.setProductId(product.getId());
            stockOut.setWarehouseId(warehouse.getId());
            stockOut.setQuantity(line.getQuantity());
            stockOut.setUnitCost(product.getCostPrice());
            stockOut.setReferenceType(REFERENCE_TYPE);
            stockOut.setReferenceId(invoice.getId());
            stockOut.setNotes("Sales invoice " + invoice.getInvoiceNumber());
            stockOut.setApproveImmediately(true);
            stockService.stockOut(stockOut);
        }

        String entryNarrative = JournalPostingNarratives.entryHeader(
                invoice.getNotes(),
                JournalPostingNarratives.SALES_INVOICE,
                invoice.getInvoiceNumber());

        Account receivableAccount = postingAccountResolver.receivable(invoice.getCustomer().getReceivableAccount());
        Account revenueAccount = postingAccountResolver.salesRevenue();
        Account taxAccount = postingAccountResolver.taxPayable();
        Account cogsAccount = postingAccountResolver.cogs();
        Account inventoryAccount = postingAccountResolver.inventory();

        BigDecimal revenueAmount = invoice.getSubtotal().subtract(invoice.getDiscountAmount()).max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        List<AccountingPostingService.JournalLineDraft> journalLines = new ArrayList<>();
        journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                .accountId(receivableAccount.getId())
                .description(JournalPostingNarratives.lineWithAccount(entryNarrative, receivableAccount, true))
                .debit(invoice.getTotalAmount())
                .credit(BigDecimal.ZERO)
                .build());
        journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                .accountId(revenueAccount.getId())
                .description(JournalPostingNarratives.lineWithAccount(entryNarrative, revenueAccount, false))
                .debit(BigDecimal.ZERO)
                .credit(revenueAmount)
                .build());

        if (invoice.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                    .accountId(taxAccount.getId())
                    .description(JournalPostingNarratives.lineWithAccount(entryNarrative, taxAccount, false)
                            + " · ضريبة | Tax")
                    .debit(BigDecimal.ZERO)
                    .credit(invoice.getTaxAmount())
                    .build());
        }

        if (totalCogs.compareTo(BigDecimal.ZERO) > 0) {
            journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                    .accountId(cogsAccount.getId())
                    .description(JournalPostingNarratives.lineWithAccount(entryNarrative, cogsAccount, true)
                            + " · COGS")
                    .debit(totalCogs)
                    .credit(BigDecimal.ZERO)
                    .build());
            journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                    .accountId(inventoryAccount.getId())
                    .description(JournalPostingNarratives.lineWithAccount(entryNarrative, inventoryAccount, false)
                            + " · Inventory")
                    .debit(BigDecimal.ZERO)
                    .credit(totalCogs)
                    .build());
        }

        JournalEntry journalEntry = accountingPostingService.createPostedJournal(
                invoice.getInvoiceDate(),
                entryNarrative,
                REFERENCE_TYPE,
                invoice.getId(),
                actor,
                journalLines
        );

        invoice.setJournalEntry(journalEntry);
        invoice.setStatus(TransactionStatus.APPROVED);
        invoice.setApprovedAt(LocalDateTime.now());
        invoice.setApprovedBy(actor);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setRemainingAmount(invoice.getTotalAmount());
        invoice = invoiceRepository.save(invoice);

        activityLogService.log(MODULE, "APPROVE", "SalesInvoice", invoice.getId(), invoice.getInvoiceNumber(),
                "Approved sales invoice " + invoice.getInvoiceNumber() + " by " + actor);
        return toDisplay(invoice);
    }

    @Transactional
    public SalesInvoiceDisplayDto cancelInvoice(Long id, String actor, String reason) {
        SalesInvoice invoice = loadInvoice(id);
        if (invoice.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Invoice is already cancelled");
        }

        if (invoice.getStatus() == TransactionStatus.APPROVED) {
            if (invoice.getWarehouse() == null) {
                throw new BusinessException("Warehouse is required to reverse stock for cancelled invoice");
            }
            Warehouse warehouse = invoice.getWarehouse();
            for (SalesInvoiceLine line : invoice.getLines()) {
                Product product = line.getProduct();
                StockMovementFormDto stockIn = new StockMovementFormDto();
                stockIn.setMovementDate(LocalDate.now());
                stockIn.setMovementType(StockMovementType.IN);
                stockIn.setProductId(product.getId());
                stockIn.setWarehouseId(warehouse.getId());
                stockIn.setQuantity(line.getQuantity());
                stockIn.setUnitCost(product.getCostPrice());
                stockIn.setReferenceType(REFERENCE_TYPE);
                stockIn.setReferenceId(invoice.getId());
                stockIn.setNotes("Cancellation reversal for sales invoice " + invoice.getInvoiceNumber());
                stockIn.setApproveImmediately(true);
                stockService.stockIn(stockIn);
            }

            JournalEntry reversalEntry = accountingPostingService.reverseJournal(
                    invoice.getJournalEntry(), actor, reason, LocalDate.now());
            invoice.setCancellationJournalEntry(reversalEntry);
        }

        invoice.setStatus(TransactionStatus.CANCELLED);
        invoice.setCancelledAt(LocalDateTime.now());
        invoice.setCancelledBy(actor);
        invoice.setRemainingAmount(BigDecimal.ZERO);
        invoice = invoiceRepository.save(invoice);

        String description = "Cancelled sales invoice " + invoice.getInvoiceNumber() + " by " + actor;
        if (reason != null && !reason.isBlank()) {
            description += ": " + reason.trim();
        }
        activityLogService.log(MODULE, "CANCEL", "SalesInvoice", invoice.getId(), invoice.getInvoiceNumber(), description);
        return toDisplay(invoice);
    }

    private void applyForm(SalesInvoice invoice, SalesInvoiceFormDto request) {
        if (request.getDueDate().isBefore(request.getInvoiceDate())) {
            throw new BusinessException("Due date cannot be before invoice date");
        }

        Customer customer = customerService.loadCustomer(request.getCustomerId());
        if (!customer.isActive()) {
            throw new BusinessException("Customer must be active");
        }

        invoice.setInvoiceDate(request.getInvoiceDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setCustomer(customer);
        invoice.setNotes(normalizeOptional(request.getNotes()));

        if (request.getOrderId() != null) {
            SalesOrder order = orderService.loadOrder(request.getOrderId());
            invoice.setOrder(order);
        } else {
            invoice.setOrder(null);
        }

        if (request.getWarehouseId() != null) {
            Warehouse warehouse = loadWarehouse(request.getWarehouseId());
            if (!warehouse.isActive()) {
                throw new BusinessException("Warehouse must be active");
            }
            invoice.setWarehouse(warehouse);
        } else {
            invoice.setWarehouse(null);
        }

        invoice.getLines().clear();
        BigDecimal lineNetSubtotal = BigDecimal.ZERO;
        BigDecimal lineTaxTotal = BigDecimal.ZERO;

        for (SalesInvoiceLineFormDto lineRequest : request.getLines()) {
            Product product = loadProduct(lineRequest.getProductId());
            if (!product.isActive()) {
                throw new BusinessException("Product must be active: " + product.getCode());
            }

            LineAmounts amounts = SalesDocumentTotalsSupport.calculateLineAmounts(
                    lineRequest.getQuantity(),
                    lineRequest.getUnitPrice(),
                    lineRequest.getDiscountPercent(),
                    lineRequest.getTaxPercent());

            SalesInvoiceLine line = SalesInvoiceLine.builder()
                    .invoice(invoice)
                    .product(product)
                    .description(normalizeOptional(lineRequest.getDescription()))
                    .quantity(lineRequest.getQuantity())
                    .unitPrice(lineRequest.getUnitPrice())
                    .discountPercent(lineRequest.getDiscountPercent() != null ? lineRequest.getDiscountPercent() : BigDecimal.ZERO)
                    .taxPercent(lineRequest.getTaxPercent() != null ? lineRequest.getTaxPercent() : BigDecimal.ZERO)
                    .lineTotal(amounts.lineTotal())
                    .build();
            invoice.getLines().add(line);
            lineNetSubtotal = lineNetSubtotal.add(amounts.netAmount());
            lineTaxTotal = lineTaxTotal.add(amounts.taxAmount());
        }

        BigDecimal headerDiscount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        DocumentAmounts documentAmounts = SalesDocumentTotalsSupport.calculateDocumentAmounts(
                lineNetSubtotal, lineTaxTotal, headerDiscount);

        invoice.setSubtotal(documentAmounts.subtotal());
        invoice.setDiscountAmount(headerDiscount);
        invoice.setTaxAmount(documentAmounts.taxAmount());
        invoice.setTotalAmount(documentAmounts.totalAmount());
        invoice.setRemainingAmount(invoice.getTotalAmount().subtract(invoice.getPaidAmount()).max(BigDecimal.ZERO));
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
            return numberingService.generateNextNumber("SALES_INVOICE");
        } catch (Exception exception) {
            return "SINV-" + System.currentTimeMillis();
        }
    }

    private Product loadProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private Warehouse loadWarehouse(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));
    }

    private SalesInvoice loadInvoice(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesInvoice", id));
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private SalesInvoiceDisplayDto toDisplay(SalesInvoice invoice) {
        Customer customer = invoice.getCustomer();
        SalesOrder order = invoice.getOrder();
        Warehouse warehouse = invoice.getWarehouse();
        return SalesInvoiceDisplayDto.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceDate(invoice.getInvoiceDate())
                .dueDate(invoice.getDueDate())
                .customerId(customer.getId())
                .customerCode(customer.getCode())
                .customerName(resolveLocalizedName(customer.getNameEn(), customer.getNameAr()))
                .orderId(order != null ? order.getId() : null)
                .orderNumber(order != null ? order.getOrderNumber() : null)
                .warehouseId(warehouse != null ? warehouse.getId() : null)
                .warehouseCode(warehouse != null ? warehouse.getCode() : null)
                .warehouseName(warehouse != null ? resolveLocalizedName(warehouse.getNameEn(), warehouse.getNameAr()) : null)
                .status(invoice.getStatus())
                .subtotal(invoice.getSubtotal())
                .discountAmount(invoice.getDiscountAmount())
                .taxAmount(invoice.getTaxAmount())
                .totalAmount(invoice.getTotalAmount())
                .paidAmount(invoice.getPaidAmount())
                .remainingAmount(invoice.getRemainingAmount())
                .notes(invoice.getNotes())
                .journalEntryId(invoice.getJournalEntry() != null ? invoice.getJournalEntry().getId() : null)
                .cancellationJournalEntryId(invoice.getCancellationJournalEntry() != null
                        ? invoice.getCancellationJournalEntry().getId() : null)
                .approvedAt(invoice.getApprovedAt())
                .approvedBy(invoice.getApprovedBy())
                .cancelledAt(invoice.getCancelledAt())
                .cancelledBy(invoice.getCancelledBy())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .lines(invoice.getLines().stream().map(this::toLineDisplay).toList())
                .build();
    }

    private SalesInvoiceLineDisplayDto toLineDisplay(SalesInvoiceLine line) {
        Product product = line.getProduct();
        return SalesInvoiceLineDisplayDto.builder()
                .id(line.getId())
                .productId(product.getId())
                .productCode(product.getCode())
                .productName(resolveLocalizedName(product.getNameEn(), product.getNameAr()))
                .description(line.getDescription())
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
                .discountPercent(line.getDiscountPercent())
                .taxPercent(line.getTaxPercent())
                .lineTotal(line.getLineTotal())
                .build();
    }

    private String resolveLocalizedName(String nameEn, String nameAr) {
        if ("ar".equalsIgnoreCase(LocaleContextHolder.getLocale().getLanguage()) && nameAr != null) {
            return nameAr;
        }
        return nameEn;
    }
}
