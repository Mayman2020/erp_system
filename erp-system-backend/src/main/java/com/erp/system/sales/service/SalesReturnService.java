package com.erp.system.sales.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.service.AccountingPostingService;
import com.erp.system.accounting.support.JournalPostingNarratives;
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
import com.erp.system.sales.domain.SalesReturn;
import com.erp.system.sales.domain.SalesReturnLine;
import com.erp.system.sales.dto.display.SalesReturnDisplayDto;
import com.erp.system.sales.dto.display.SalesReturnLineDisplayDto;
import com.erp.system.sales.dto.form.SalesReturnFormDto;
import com.erp.system.sales.dto.form.SalesReturnLineFormDto;
import com.erp.system.sales.repository.SalesInvoiceRepository;
import com.erp.system.sales.repository.SalesReturnRepository;
import com.erp.system.sales.support.SalesDocumentTotalsSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesReturnService {

    private static final String MODULE = "SALES";
    private static final String REFERENCE_TYPE = "SALES_RETURN";
    private static final String DEFAULT_RECEIVABLE_CODE = "1200";
    private static final String REVENUE_CODE = "4100";
    private static final String TAX_PAYABLE_CODE = "2210";
    private static final String COGS_CODE = "5130";
    private static final String INVENTORY_CODE = "1300";

    private final SalesReturnRepository returnRepository;
    private final SalesInvoiceRepository invoiceRepository;
    private final CustomerService customerService;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockService stockService;
    private final AccountRepository accountRepository;
    private final NumberingService numberingService;
    private final AccountingPostingService accountingPostingService;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<SalesReturnDisplayDto> getReturns(TransactionStatus status, String search,
                                                  LocalDate fromDate, LocalDate toDate) {
        List<SalesReturn> returns = status == null
                ? returnRepository.findAllByOrderByReturnDateDescIdDesc()
                : returnRepository.findByStatusOrderByReturnDateDescIdDesc(status);

        String normalizedSearch = search == null || search.isBlank() ? null : search.trim().toLowerCase();
        return returns.stream()
                .filter(r -> fromDate == null || !r.getReturnDate().isBefore(fromDate))
                .filter(r -> toDate == null || !r.getReturnDate().isAfter(toDate))
                .filter(r -> normalizedSearch == null
                        || r.getReturnNumber().toLowerCase().contains(normalizedSearch)
                        || r.getCustomer().getCode().toLowerCase().contains(normalizedSearch)
                        || r.getCustomer().getNameEn().toLowerCase().contains(normalizedSearch))
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public SalesReturnDisplayDto getReturn(Long id) {
        return toDisplay(loadReturn(id));
    }

    @Transactional
    public SalesReturnDisplayDto createReturn(SalesReturnFormDto request) {
        SalesReturn salesReturn = SalesReturn.builder()
                .returnNumber(resolveReturnNumber(request.getReturnNumber()))
                .status(TransactionStatus.DRAFT)
                .lines(new ArrayList<>())
                .build();
        applyForm(salesReturn, request);
        salesReturn = returnRepository.save(salesReturn);

        activityLogService.log(MODULE, "CREATE", "SalesReturn", salesReturn.getId(), salesReturn.getReturnNumber(),
                "Created sales return " + salesReturn.getReturnNumber());
        return toDisplay(salesReturn);
    }

    @Transactional
    public SalesReturnDisplayDto updateReturn(Long id, SalesReturnFormDto request) {
        SalesReturn salesReturn = loadReturn(id);
        if (salesReturn.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft returns can be edited");
        }
        applyForm(salesReturn, request);
        salesReturn = returnRepository.save(salesReturn);

        activityLogService.log(MODULE, "UPDATE", "SalesReturn", salesReturn.getId(), salesReturn.getReturnNumber(),
                "Updated sales return " + salesReturn.getReturnNumber());
        return toDisplay(salesReturn);
    }

    @Transactional
    public void deleteReturn(Long id) {
        SalesReturn salesReturn = loadReturn(id);
        if (salesReturn.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft returns can be deleted");
        }
        returnRepository.delete(salesReturn);
        activityLogService.log(MODULE, "DELETE", "SalesReturn", salesReturn.getId(), salesReturn.getReturnNumber(),
                "Deleted sales return " + salesReturn.getReturnNumber());
    }

    @Transactional
    public SalesReturnDisplayDto approveReturn(Long id, String actor) {
        SalesReturn salesReturn = loadReturn(id);
        if (salesReturn.getStatus() == TransactionStatus.APPROVED) {
            return toDisplay(salesReturn);
        }
        if (salesReturn.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Cancelled returns cannot be approved");
        }
        if (salesReturn.getLines().isEmpty()) {
            throw new BusinessException("Return must have at least one line");
        }
        if (salesReturn.getWarehouse() == null) {
            throw new BusinessException("Warehouse is required to approve a sales return");
        }

        Warehouse warehouse = salesReturn.getWarehouse();
        BigDecimal totalCogs = BigDecimal.ZERO;

        for (SalesReturnLine line : salesReturn.getLines()) {
            Product product = line.getProduct();
            BigDecimal lineCogs = product.getCostPrice().multiply(line.getQuantity()).setScale(2, RoundingMode.HALF_UP);
            totalCogs = totalCogs.add(lineCogs);

            StockMovementFormDto stockIn = new StockMovementFormDto();
            stockIn.setMovementDate(salesReturn.getReturnDate());
            stockIn.setMovementType(StockMovementType.IN);
            stockIn.setProductId(product.getId());
            stockIn.setWarehouseId(warehouse.getId());
            stockIn.setQuantity(line.getQuantity());
            stockIn.setUnitCost(product.getCostPrice());
            stockIn.setReferenceType(REFERENCE_TYPE);
            stockIn.setReferenceId(salesReturn.getId());
            stockIn.setNotes("Sales return " + salesReturn.getReturnNumber());
            stockIn.setApproveImmediately(true);
            stockService.stockIn(stockIn);
        }

        String entryNarrative = JournalPostingNarratives.entryHeader(
                salesReturn.getNotes(),
                "مرتجع مبيعات | Sales return",
                salesReturn.getReturnNumber());

        Account receivableAccount = resolveReceivableAccount(salesReturn.getCustomer());
        Account revenueAccount = resolveAccountByCode(REVENUE_CODE, "Revenue");
        Account taxAccount = resolveTaxAccount(revenueAccount);
        Account cogsAccount = resolveAccountByCode(COGS_CODE, "COGS");
        Account inventoryAccount = resolveAccountByCode(INVENTORY_CODE, "Inventory");

        BigDecimal revenueAmount = salesReturn.getSubtotal().setScale(2, RoundingMode.HALF_UP);

        List<AccountingPostingService.JournalLineDraft> journalLines = new ArrayList<>();
        journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                .accountId(receivableAccount.getId())
                .description(JournalPostingNarratives.lineWithAccount(entryNarrative, receivableAccount, false))
                .debit(BigDecimal.ZERO)
                .credit(salesReturn.getTotalAmount())
                .build());
        journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                .accountId(revenueAccount.getId())
                .description(JournalPostingNarratives.lineWithAccount(entryNarrative, revenueAccount, true))
                .debit(revenueAmount)
                .credit(BigDecimal.ZERO)
                .build());

        if (salesReturn.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                    .accountId(taxAccount.getId())
                    .description(JournalPostingNarratives.lineWithAccount(entryNarrative, taxAccount, true)
                            + " · ضريبة | Tax")
                    .debit(salesReturn.getTaxAmount())
                    .credit(BigDecimal.ZERO)
                    .build());
        }

        if (totalCogs.compareTo(BigDecimal.ZERO) > 0) {
            journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                    .accountId(inventoryAccount.getId())
                    .description(JournalPostingNarratives.lineWithAccount(entryNarrative, inventoryAccount, true)
                            + " · Inventory")
                    .debit(totalCogs)
                    .credit(BigDecimal.ZERO)
                    .build());
            journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                    .accountId(cogsAccount.getId())
                    .description(JournalPostingNarratives.lineWithAccount(entryNarrative, cogsAccount, false)
                            + " · COGS")
                    .debit(BigDecimal.ZERO)
                    .credit(totalCogs)
                    .build());
        }

        JournalEntry journalEntry = accountingPostingService.createPostedJournal(
                salesReturn.getReturnDate(),
                entryNarrative,
                REFERENCE_TYPE,
                salesReturn.getId(),
                actor,
                journalLines
        );

        salesReturn.setJournalEntry(journalEntry);
        salesReturn.setStatus(TransactionStatus.APPROVED);
        salesReturn = returnRepository.save(salesReturn);

        activityLogService.log(MODULE, "APPROVE", "SalesReturn", salesReturn.getId(), salesReturn.getReturnNumber(),
                "Approved sales return " + salesReturn.getReturnNumber() + " by " + actor);
        return toDisplay(salesReturn);
    }

    @Transactional
    public SalesReturnDisplayDto cancelReturn(Long id, String actor, String reason) {
        SalesReturn salesReturn = loadReturn(id);
        if (salesReturn.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Return is already cancelled");
        }

        if (salesReturn.getStatus() == TransactionStatus.APPROVED) {
            if (salesReturn.getWarehouse() == null) {
                throw new BusinessException("Warehouse is required to reverse stock for cancelled return");
            }
            Warehouse warehouse = salesReturn.getWarehouse();
            for (SalesReturnLine line : salesReturn.getLines()) {
                Product product = line.getProduct();
                StockMovementFormDto stockOut = new StockMovementFormDto();
                stockOut.setMovementDate(LocalDate.now());
                stockOut.setMovementType(StockMovementType.OUT);
                stockOut.setProductId(product.getId());
                stockOut.setWarehouseId(warehouse.getId());
                stockOut.setQuantity(line.getQuantity());
                stockOut.setUnitCost(product.getCostPrice());
                stockOut.setReferenceType(REFERENCE_TYPE);
                stockOut.setReferenceId(salesReturn.getId());
                stockOut.setNotes("Cancellation reversal for sales return " + salesReturn.getReturnNumber());
                stockOut.setApproveImmediately(true);
                stockService.stockOut(stockOut);
            }

            accountingPostingService.reverseJournal(salesReturn.getJournalEntry(), actor, reason, LocalDate.now());
        }

        salesReturn.setStatus(TransactionStatus.CANCELLED);
        salesReturn = returnRepository.save(salesReturn);

        String description = "Cancelled sales return " + salesReturn.getReturnNumber() + " by " + actor;
        if (reason != null && !reason.isBlank()) {
            description += ": " + reason.trim();
        }
        activityLogService.log(MODULE, "CANCEL", "SalesReturn", salesReturn.getId(), salesReturn.getReturnNumber(), description);
        return toDisplay(salesReturn);
    }

    private void applyForm(SalesReturn salesReturn, SalesReturnFormDto request) {
        Customer customer = customerService.loadCustomer(request.getCustomerId());
        if (!customer.isActive()) {
            throw new BusinessException("Customer must be active");
        }

        salesReturn.setReturnDate(request.getReturnDate());
        salesReturn.setCustomer(customer);
        salesReturn.setNotes(normalizeOptional(request.getNotes()));

        if (request.getInvoiceId() != null) {
            SalesInvoice invoice = invoiceRepository.findById(request.getInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("SalesInvoice", request.getInvoiceId()));
            salesReturn.setInvoice(invoice);
        } else {
            salesReturn.setInvoice(null);
        }

        if (request.getWarehouseId() != null) {
            Warehouse warehouse = loadWarehouse(request.getWarehouseId());
            if (!warehouse.isActive()) {
                throw new BusinessException("Warehouse must be active");
            }
            salesReturn.setWarehouse(warehouse);
        } else {
            salesReturn.setWarehouse(null);
        }

        salesReturn.getLines().clear();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (SalesReturnLineFormDto lineRequest : request.getLines()) {
            Product product = loadProduct(lineRequest.getProductId());
            if (!product.isActive()) {
                throw new BusinessException("Product must be active: " + product.getCode());
            }

            BigDecimal lineTotal = SalesDocumentTotalsSupport.calculateReturnLineTotal(
                    lineRequest.getQuantity(), lineRequest.getUnitPrice());

            SalesReturnLine line = SalesReturnLine.builder()
                    .salesReturn(salesReturn)
                    .product(product)
                    .quantity(lineRequest.getQuantity())
                    .unitPrice(lineRequest.getUnitPrice())
                    .lineTotal(lineTotal)
                    .build();
            salesReturn.getLines().add(line);
            subtotal = subtotal.add(lineTotal);
        }

        BigDecimal taxAmount = request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO;
        if (taxAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Tax amount cannot be negative");
        }
        taxAmount = taxAmount.setScale(2, RoundingMode.HALF_UP);

        salesReturn.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        salesReturn.setTaxAmount(taxAmount);
        salesReturn.setTotalAmount(subtotal.add(taxAmount).setScale(2, RoundingMode.HALF_UP));
    }

    private Account resolveReceivableAccount(Customer customer) {
        Account receivable = customer.getReceivableAccount();
        if (receivable != null && receivable.isActive()) {
            return receivable;
        }
        return resolveAccountByCode(DEFAULT_RECEIVABLE_CODE, "Accounts Receivable");
    }

    private Account resolveTaxAccount(Account revenueFallback) {
        return accountRepository.findByCode(TAX_PAYABLE_CODE)
                .filter(Account::isActive)
                .orElse(revenueFallback);
    }

    private Account resolveAccountByCode(String code, String label) {
        return accountRepository.findByCode(code)
                .filter(Account::isActive)
                .orElseThrow(() -> new BusinessException("Active " + label + " account (" + code + ") is required"));
    }

    private String resolveReturnNumber(String returnNumber) {
        String normalized = normalizeOptional(returnNumber);
        if (normalized != null) {
            if (returnRepository.existsByReturnNumberIgnoreCase(normalized)) {
                throw new BusinessException("Return number already exists");
            }
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("SALES_RETURN");
        } catch (Exception exception) {
            return "SR-" + System.currentTimeMillis();
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

    private SalesReturn loadReturn(Long id) {
        return returnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesReturn", id));
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private SalesReturnDisplayDto toDisplay(SalesReturn salesReturn) {
        Customer customer = salesReturn.getCustomer();
        SalesInvoice invoice = salesReturn.getInvoice();
        Warehouse warehouse = salesReturn.getWarehouse();
        return SalesReturnDisplayDto.builder()
                .id(salesReturn.getId())
                .returnNumber(salesReturn.getReturnNumber())
                .returnDate(salesReturn.getReturnDate())
                .customerId(customer.getId())
                .customerCode(customer.getCode())
                .customerName(resolveLocalizedName(customer.getNameEn(), customer.getNameAr()))
                .invoiceId(invoice != null ? invoice.getId() : null)
                .invoiceNumber(invoice != null ? invoice.getInvoiceNumber() : null)
                .warehouseId(warehouse != null ? warehouse.getId() : null)
                .warehouseCode(warehouse != null ? warehouse.getCode() : null)
                .warehouseName(warehouse != null ? resolveLocalizedName(warehouse.getNameEn(), warehouse.getNameAr()) : null)
                .status(salesReturn.getStatus())
                .subtotal(salesReturn.getSubtotal())
                .taxAmount(salesReturn.getTaxAmount())
                .totalAmount(salesReturn.getTotalAmount())
                .notes(salesReturn.getNotes())
                .journalEntryId(salesReturn.getJournalEntry() != null ? salesReturn.getJournalEntry().getId() : null)
                .createdAt(salesReturn.getCreatedAt())
                .updatedAt(salesReturn.getUpdatedAt())
                .lines(salesReturn.getLines().stream().map(this::toLineDisplay).toList())
                .build();
    }

    private SalesReturnLineDisplayDto toLineDisplay(SalesReturnLine line) {
        Product product = line.getProduct();
        return SalesReturnLineDisplayDto.builder()
                .id(line.getId())
                .productId(product.getId())
                .productCode(product.getCode())
                .productName(resolveLocalizedName(product.getNameEn(), product.getNameAr()))
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
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
