package com.erp.system.purchases.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.accounting.repository.JournalEntryRepository;
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
import com.erp.system.inventory.dto.form.StockMovementFormDto;
import com.erp.system.inventory.repository.ProductRepository;
import com.erp.system.inventory.service.StockService;
import com.erp.system.purchases.domain.PurchaseReturn;
import com.erp.system.purchases.domain.PurchaseReturnLine;
import com.erp.system.purchases.dto.display.PurchaseReturnDisplayDto;
import com.erp.system.purchases.dto.display.PurchaseReturnLineDisplayDto;
import com.erp.system.purchases.dto.form.PurchaseReturnFormDto;
import com.erp.system.purchases.dto.form.PurchaseReturnLineInputDto;
import com.erp.system.purchases.repository.PurchaseReturnLineRepository;
import com.erp.system.purchases.repository.PurchaseReturnRepository;
import com.erp.system.sales.support.SalesDocumentTotalsSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseReturnService {

    private static final String MODULE = "PURCHASES";

    private final PurchaseReturnRepository purchaseReturnRepository;
    private final PurchaseReturnLineRepository purchaseReturnLineRepository;
    private final ProductRepository productRepository;
    private final PostingAccountResolver postingAccountResolver;
    private final JournalEntryRepository journalEntryRepository;
    private final StockService stockService;
    private final AccountingPostingService accountingPostingService;
    private final NumberingService numberingService;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<PurchaseReturnDisplayDto> getAll() {
        return purchaseReturnRepository.findAllByOrderByIdDesc().stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public PurchaseReturnDisplayDto getById(Long id) {
        return toDisplay(loadPurchaseReturn(id));
    }

    @Transactional
    public PurchaseReturnDisplayDto create(PurchaseReturnFormDto request) {
        PurchaseReturn purchaseReturn = new PurchaseReturn();
        applyForm(purchaseReturn, request);
        purchaseReturn.setReturnNumber(resolveNumber(request.getReturnNumber()));
        purchaseReturn.setStatus(TransactionStatus.DRAFT);
        purchaseReturn = purchaseReturnRepository.save(purchaseReturn);
        replaceLines(purchaseReturn.getId(), request.getLines());
        purchaseReturn = purchaseReturnRepository.save(purchaseReturn);
        activityLogService.log(MODULE, "CREATE", "PurchaseReturn", purchaseReturn.getId(), purchaseReturn.getReturnNumber(),
                "Created purchase return " + purchaseReturn.getReturnNumber());
        return toDisplay(purchaseReturn);
    }

    @Transactional
    public PurchaseReturnDisplayDto update(Long id, PurchaseReturnFormDto request) {
        PurchaseReturn purchaseReturn = loadPurchaseReturn(id);
        if (purchaseReturn.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft returns can be edited");
        }
        applyForm(purchaseReturn, request);
        purchaseReturn = purchaseReturnRepository.save(purchaseReturn);
        replaceLines(purchaseReturn.getId(), request.getLines());
        purchaseReturn = purchaseReturnRepository.save(purchaseReturn);
        activityLogService.log(MODULE, "UPDATE", "PurchaseReturn", purchaseReturn.getId(), purchaseReturn.getReturnNumber(),
                "Updated purchase return " + purchaseReturn.getReturnNumber());
        return toDisplay(purchaseReturn);
    }

    @Transactional
    public PurchaseReturnDisplayDto approve(Long id, String actor) {
        PurchaseReturn purchaseReturn = loadPurchaseReturn(id);
        if (purchaseReturn.getStatus() == TransactionStatus.APPROVED) {
            return toDisplay(purchaseReturn);
        }
        if (purchaseReturn.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Cancelled return cannot be approved");
        }
        if (purchaseReturn.getWarehouseId() == null) {
            throw new BusinessException("Warehouse is required to approve purchase return");
        }

        List<PurchaseReturnLine> lines = purchaseReturnLineRepository.findByReturnIdOrderByIdAsc(purchaseReturn.getId());
        if (lines.isEmpty()) {
            throw new BusinessException("Return must have at least one line");
        }

        for (PurchaseReturnLine line : lines) {
            Product product = productRepository.findById(line.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", line.getProductId()));
            StockMovementFormDto stockOut = new StockMovementFormDto();
            stockOut.setMovementDate(purchaseReturn.getReturnDate());
            stockOut.setMovementType(StockMovementType.OUT);
            stockOut.setProductId(product.getId());
            stockOut.setWarehouseId(purchaseReturn.getWarehouseId());
            stockOut.setQuantity(line.getQuantity());
            stockOut.setUnitCost(product.getCostPrice());
            stockOut.setReferenceType("PURCHASE_RETURN");
            stockOut.setReferenceId(purchaseReturn.getId());
            stockOut.setNotes("Purchase return " + purchaseReturn.getReturnNumber());
            stockOut.setApproveImmediately(true);
            stockService.stockOut(stockOut);
        }

        Account payableAccount = postingAccountResolver.accountsPayable();
        Account inventoryAccount = postingAccountResolver.inventory();
        Account taxAccount = postingAccountResolver.taxPayable();

        BigDecimal taxAmount = purchaseReturn.getTaxAmount() == null ? BigDecimal.ZERO : purchaseReturn.getTaxAmount();
        BigDecimal inventoryAmount = purchaseReturn.getTotalAmount().subtract(taxAmount).max(BigDecimal.ZERO);
        String narrative = JournalPostingNarratives.entryHeader(
                purchaseReturn.getNotes(),
                "مرتجع مشتريات | Purchase return",
                purchaseReturn.getReturnNumber()
        );

        List<AccountingPostingService.JournalLineDraft> journalLines = new ArrayList<>();
        journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                .accountId(payableAccount.getId())
                .description(JournalPostingNarratives.lineWithAccount(narrative, payableAccount, true))
                .debit(purchaseReturn.getTotalAmount())
                .credit(BigDecimal.ZERO)
                .build());
        journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                .accountId(inventoryAccount.getId())
                .description(JournalPostingNarratives.lineWithAccount(narrative, inventoryAccount, false))
                .debit(BigDecimal.ZERO)
                .credit(inventoryAmount)
                .build());
        if (taxAmount.compareTo(BigDecimal.ZERO) > 0) {
            journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                    .accountId(taxAccount.getId())
                    .description(JournalPostingNarratives.lineWithAccount(narrative, taxAccount, false))
                    .debit(BigDecimal.ZERO)
                    .credit(taxAmount)
                    .build());
        }

        JournalEntry journalEntry = accountingPostingService.createPostedJournal(
                purchaseReturn.getReturnDate(),
                narrative,
                "PURCHASE_RETURN",
                purchaseReturn.getId(),
                actor,
                journalLines
        );

        purchaseReturn.setJournalEntryId(journalEntry.getId());
        purchaseReturn.setStatus(TransactionStatus.APPROVED);
        purchaseReturn.setUpdatedBy(actor);
        purchaseReturn = purchaseReturnRepository.save(purchaseReturn);
        activityLogService.log(MODULE, "APPROVE", "PurchaseReturn", purchaseReturn.getId(), purchaseReturn.getReturnNumber(),
                "Approved purchase return " + purchaseReturn.getReturnNumber());
        return toDisplay(purchaseReturn);
    }

    @Transactional
    public PurchaseReturnDisplayDto cancel(Long id, String actor, String reason) {
        PurchaseReturn purchaseReturn = loadPurchaseReturn(id);
        if (purchaseReturn.getStatus() == TransactionStatus.CANCELLED) {
            return toDisplay(purchaseReturn);
        }
        if (purchaseReturn.getJournalEntryId() != null) {
            final Long journalEntryId = purchaseReturn.getJournalEntryId();
            JournalEntry original = journalEntryRepository.findById(journalEntryId)
                    .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", journalEntryId));
            accountingPostingService.reverseJournal(original, actor, reason, LocalDate.now());
        }
        purchaseReturn.setStatus(TransactionStatus.CANCELLED);
        purchaseReturn.setUpdatedBy(actor);
        purchaseReturn = purchaseReturnRepository.save(purchaseReturn);
        activityLogService.log(MODULE, "CANCEL", "PurchaseReturn", purchaseReturn.getId(), purchaseReturn.getReturnNumber(),
                "Cancelled purchase return " + purchaseReturn.getReturnNumber());
        return toDisplay(purchaseReturn);
    }

    @Transactional
    public void delete(Long id) {
        PurchaseReturn purchaseReturn = loadPurchaseReturn(id);
        if (purchaseReturn.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft returns can be deleted");
        }
        purchaseReturnRepository.delete(purchaseReturn);
        activityLogService.log(MODULE, "DELETE", "PurchaseReturn", id, purchaseReturn.getReturnNumber(),
                "Deleted purchase return " + purchaseReturn.getReturnNumber());
    }

    private String resolveNumber(String requested) {
        String normalized = requested == null ? null : requested.trim();
        if (normalized != null && !normalized.isEmpty()) {
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("PURCHASE_RETURN");
        } catch (Exception ex) {
            return "PRET-" + System.currentTimeMillis();
        }
    }

    private void applyForm(PurchaseReturn purchaseReturn, PurchaseReturnFormDto request) {
        if (request.getLines() == null || request.getLines().isEmpty()) {
            throw new BusinessException("Return must have at least one line");
        }
        purchaseReturn.setReturnDate(request.getReturnDate());
        purchaseReturn.setSupplierId(request.getSupplierId());
        purchaseReturn.setInvoiceId(request.getInvoiceId());
        purchaseReturn.setWarehouseId(request.getWarehouseId());
        purchaseReturn.setNotes(request.getNotes());

        BigDecimal subtotal = BigDecimal.ZERO;
        for (PurchaseReturnLineInputDto lineRequest : request.getLines()) {
            subtotal = subtotal.add(SalesDocumentTotalsSupport.calculateReturnLineTotal(
                    lineRequest.getQuantity(), lineRequest.getUnitPrice()));
        }
        BigDecimal tax = request.getTaxAmount() == null ? BigDecimal.ZERO : request.getTaxAmount();
        purchaseReturn.setSubtotal(subtotal);
        purchaseReturn.setTaxAmount(tax);
        purchaseReturn.setTotalAmount(subtotal.add(tax));
    }

    private void replaceLines(Long returnId, List<PurchaseReturnLineInputDto> lineRequests) {
        List<PurchaseReturnLine> existing = purchaseReturnLineRepository.findByReturnIdOrderByIdAsc(returnId);
        if (!existing.isEmpty()) {
            purchaseReturnLineRepository.deleteAll(existing);
        }
        for (PurchaseReturnLineInputDto lineRequest : lineRequests) {
            if (!productRepository.existsById(lineRequest.getProductId())) {
                throw new BusinessException("Product not found: " + lineRequest.getProductId());
            }
            BigDecimal lineTotal = SalesDocumentTotalsSupport.calculateReturnLineTotal(
                    lineRequest.getQuantity(), lineRequest.getUnitPrice());
            PurchaseReturnLine line = PurchaseReturnLine.builder()
                    .returnId(returnId)
                    .productId(lineRequest.getProductId())
                    .quantity(lineRequest.getQuantity())
                    .unitPrice(lineRequest.getUnitPrice())
                    .lineTotal(lineTotal)
                    .build();
            purchaseReturnLineRepository.save(line);
        }
    }

    private PurchaseReturn loadPurchaseReturn(Long id) {
        return purchaseReturnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseReturn", id));
    }

    private PurchaseReturnDisplayDto toDisplay(PurchaseReturn purchaseReturn) {
        List<PurchaseReturnLineDisplayDto> lines = purchaseReturnLineRepository.findByReturnIdOrderByIdAsc(purchaseReturn.getId())
                .stream()
                .map(line -> PurchaseReturnLineDisplayDto.builder()
                        .id(line.getId())
                        .returnId(line.getReturnId())
                        .productId(line.getProductId())
                        .quantity(line.getQuantity())
                        .unitPrice(line.getUnitPrice())
                        .lineTotal(line.getLineTotal())
                        .build())
                .toList();
        return PurchaseReturnDisplayDto.builder()
                .id(purchaseReturn.getId())
                .returnNumber(purchaseReturn.getReturnNumber())
                .returnDate(purchaseReturn.getReturnDate())
                .supplierId(purchaseReturn.getSupplierId())
                .invoiceId(purchaseReturn.getInvoiceId())
                .warehouseId(purchaseReturn.getWarehouseId())
                .status(purchaseReturn.getStatus())
                .subtotal(purchaseReturn.getSubtotal())
                .taxAmount(purchaseReturn.getTaxAmount())
                .totalAmount(purchaseReturn.getTotalAmount())
                .notes(purchaseReturn.getNotes())
                .journalEntryId(purchaseReturn.getJournalEntryId())
                .lines(lines)
                .createdAt(purchaseReturn.getCreatedAt())
                .updatedAt(purchaseReturn.getUpdatedAt())
                .build();
    }
}
