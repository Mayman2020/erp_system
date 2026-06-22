package com.erp.system.purchases.service;

import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.purchases.domain.PurchaseReturn;
import com.erp.system.purchases.dto.display.PurchaseReturnDisplayDto;
import com.erp.system.purchases.dto.form.PurchaseReturnFormDto;
import com.erp.system.purchases.repository.PurchaseReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseReturnService {

    private static final String MODULE = "PURCHASES";

    private final PurchaseReturnRepository purchaseReturnRepository;
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
        purchaseReturn.setStatus(TransactionStatus.DRAFT);
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
        activityLogService.log(MODULE, "UPDATE", "PurchaseReturn", purchaseReturn.getId(), purchaseReturn.getReturnNumber(),
                "Updated purchase return " + purchaseReturn.getReturnNumber());
        return toDisplay(purchaseReturn);
    }

    @Transactional
    public PurchaseReturnDisplayDto approve(Long id, String actor) {
        PurchaseReturn purchaseReturn = loadPurchaseReturn(id);
        if (purchaseReturn.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Cancelled return cannot be approved");
        }
        purchaseReturn.setStatus(TransactionStatus.APPROVED);
        purchaseReturn = purchaseReturnRepository.save(purchaseReturn);
        activityLogService.log(MODULE, "APPROVE", "PurchaseReturn", purchaseReturn.getId(), purchaseReturn.getReturnNumber(),
                "Approved purchase return " + purchaseReturn.getReturnNumber());
        return toDisplay(purchaseReturn);
    }

    @Transactional
    public PurchaseReturnDisplayDto cancel(Long id, String actor, String reason) {
        PurchaseReturn purchaseReturn = loadPurchaseReturn(id);
        purchaseReturn.setStatus(TransactionStatus.CANCELLED);
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

    private PurchaseReturn loadPurchaseReturn(Long id) {
        return purchaseReturnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseReturn", id));
    }

    private void applyForm(PurchaseReturn purchaseReturn, PurchaseReturnFormDto request) {
        purchaseReturn.setReturnNumber(request.getReturnNumber());
        purchaseReturn.setReturnDate(request.getReturnDate());
        purchaseReturn.setSupplierId(request.getSupplierId());
        purchaseReturn.setInvoiceId(request.getInvoiceId());
        purchaseReturn.setWarehouseId(request.getWarehouseId());
        purchaseReturn.setSubtotal(request.getSubtotal());
        purchaseReturn.setTaxAmount(request.getTaxAmount() == null ? BigDecimal.ZERO : request.getTaxAmount());
        purchaseReturn.setTotalAmount(request.getTotalAmount());
        purchaseReturn.setNotes(request.getNotes());
    }

    private PurchaseReturnDisplayDto toDisplay(PurchaseReturn purchaseReturn) {
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
                .createdAt(purchaseReturn.getCreatedAt())
                .updatedAt(purchaseReturn.getUpdatedAt())
                .build();
    }
}
