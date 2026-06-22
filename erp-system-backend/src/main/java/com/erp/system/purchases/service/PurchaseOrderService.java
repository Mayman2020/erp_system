package com.erp.system.purchases.service;

import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.purchases.domain.PurchaseOrder;
import com.erp.system.purchases.dto.display.PurchaseOrderDisplayDto;
import com.erp.system.purchases.dto.form.PurchaseOrderFormDto;
import com.erp.system.purchases.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private static final String MODULE = "PURCHASES";

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<PurchaseOrderDisplayDto> getAll() {
        return purchaseOrderRepository.findAllByOrderByIdDesc().stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public PurchaseOrderDisplayDto getById(Long id) {
        return toDisplay(loadPurchaseOrder(id));
    }

    @Transactional
    public PurchaseOrderDisplayDto create(PurchaseOrderFormDto request) {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        applyForm(purchaseOrder, request);
        purchaseOrder.setStatus(TransactionStatus.DRAFT);
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        activityLogService.log(MODULE, "CREATE", "PurchaseOrder", purchaseOrder.getId(), purchaseOrder.getOrderNumber(),
                "Created purchase order " + purchaseOrder.getOrderNumber());
        return toDisplay(purchaseOrder);
    }

    @Transactional
    public PurchaseOrderDisplayDto update(Long id, PurchaseOrderFormDto request) {
        PurchaseOrder purchaseOrder = loadPurchaseOrder(id);
        if (purchaseOrder.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft orders can be edited");
        }
        applyForm(purchaseOrder, request);
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        activityLogService.log(MODULE, "UPDATE", "PurchaseOrder", purchaseOrder.getId(), purchaseOrder.getOrderNumber(),
                "Updated purchase order " + purchaseOrder.getOrderNumber());
        return toDisplay(purchaseOrder);
    }

    @Transactional
    public PurchaseOrderDisplayDto approve(Long id, String actor) {
        PurchaseOrder purchaseOrder = loadPurchaseOrder(id);
        if (purchaseOrder.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Cancelled order cannot be approved");
        }
        purchaseOrder.setStatus(TransactionStatus.APPROVED);
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        activityLogService.log(MODULE, "APPROVE", "PurchaseOrder", purchaseOrder.getId(), purchaseOrder.getOrderNumber(),
                "Approved purchase order " + purchaseOrder.getOrderNumber());
        return toDisplay(purchaseOrder);
    }

    @Transactional
    public PurchaseOrderDisplayDto cancel(Long id, String actor, String reason) {
        PurchaseOrder purchaseOrder = loadPurchaseOrder(id);
        purchaseOrder.setStatus(TransactionStatus.CANCELLED);
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        activityLogService.log(MODULE, "CANCEL", "PurchaseOrder", purchaseOrder.getId(), purchaseOrder.getOrderNumber(),
                "Cancelled purchase order " + purchaseOrder.getOrderNumber());
        return toDisplay(purchaseOrder);
    }

    @Transactional
    public void delete(Long id) {
        PurchaseOrder purchaseOrder = loadPurchaseOrder(id);
        if (purchaseOrder.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft orders can be deleted");
        }
        purchaseOrderRepository.delete(purchaseOrder);
        activityLogService.log(MODULE, "DELETE", "PurchaseOrder", id, purchaseOrder.getOrderNumber(),
                "Deleted purchase order " + purchaseOrder.getOrderNumber());
    }

    private PurchaseOrder loadPurchaseOrder(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));
    }

    private void applyForm(PurchaseOrder purchaseOrder, PurchaseOrderFormDto request) {
        purchaseOrder.setOrderNumber(request.getOrderNumber());
        purchaseOrder.setOrderDate(request.getOrderDate());
        purchaseOrder.setSupplierId(request.getSupplierId());
        purchaseOrder.setWarehouseId(request.getWarehouseId());
        purchaseOrder.setSubtotal(request.getSubtotal());
        purchaseOrder.setDiscountAmount(request.getDiscountAmount() == null ? BigDecimal.ZERO : request.getDiscountAmount());
        purchaseOrder.setTaxAmount(request.getTaxAmount() == null ? BigDecimal.ZERO : request.getTaxAmount());
        purchaseOrder.setTotalAmount(request.getTotalAmount());
        purchaseOrder.setNotes(request.getNotes());
    }

    private PurchaseOrderDisplayDto toDisplay(PurchaseOrder purchaseOrder) {
        return PurchaseOrderDisplayDto.builder()
                .id(purchaseOrder.getId())
                .orderNumber(purchaseOrder.getOrderNumber())
                .orderDate(purchaseOrder.getOrderDate())
                .supplierId(purchaseOrder.getSupplierId())
                .warehouseId(purchaseOrder.getWarehouseId())
                .status(purchaseOrder.getStatus())
                .subtotal(purchaseOrder.getSubtotal())
                .discountAmount(purchaseOrder.getDiscountAmount())
                .taxAmount(purchaseOrder.getTaxAmount())
                .totalAmount(purchaseOrder.getTotalAmount())
                .notes(purchaseOrder.getNotes())
                .createdAt(purchaseOrder.getCreatedAt())
                .updatedAt(purchaseOrder.getUpdatedAt())
                .build();
    }
}
