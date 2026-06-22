package com.erp.system.purchases.service;

import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.purchases.domain.PurchaseOrderLine;
import com.erp.system.purchases.dto.display.PurchaseOrderLineDisplayDto;
import com.erp.system.purchases.dto.form.PurchaseOrderLineFormDto;
import com.erp.system.purchases.repository.PurchaseOrderLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PurchaseOrderLineService {

    private static final String MODULE = "PURCHASES";

    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<PurchaseOrderLineDisplayDto> getAll() {
        return purchaseOrderLineRepository.findAllByOrderByIdDesc().stream()
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseOrderLineDisplayDto getById(Long id) {
        return toDisplay(loadPurchaseOrderLine(id));
    }

    @Transactional
    public PurchaseOrderLineDisplayDto create(PurchaseOrderLineFormDto request) {
        PurchaseOrderLine purchaseOrderLine = new PurchaseOrderLine();
        applyForm(purchaseOrderLine, request);
        purchaseOrderLine = purchaseOrderLineRepository.save(purchaseOrderLine);
        activityLogService.log(MODULE, "CREATE", "PurchaseOrderLine", purchaseOrderLine.getId(), String.valueOf(purchaseOrderLine.getId()),
                "Created PurchaseOrderLine " + purchaseOrderLine.getId());
        return toDisplay(purchaseOrderLine);
    }

    @Transactional
    public PurchaseOrderLineDisplayDto update(Long id, PurchaseOrderLineFormDto request) {
        PurchaseOrderLine purchaseOrderLine = loadPurchaseOrderLine(id);
        applyForm(purchaseOrderLine, request);
        purchaseOrderLine = purchaseOrderLineRepository.save(purchaseOrderLine);
        activityLogService.log(MODULE, "UPDATE", "PurchaseOrderLine", purchaseOrderLine.getId(), String.valueOf(purchaseOrderLine.getId()),
                "Updated PurchaseOrderLine " + purchaseOrderLine.getId());
        return toDisplay(purchaseOrderLine);
    }

    @Transactional
    public void delete(Long id) {
        PurchaseOrderLine purchaseOrderLine = loadPurchaseOrderLine(id);
        purchaseOrderLineRepository.delete(purchaseOrderLine);
        activityLogService.log(MODULE, "DELETE", "PurchaseOrderLine", id, String.valueOf(id),
                "Deleted PurchaseOrderLine " + id);
    }

    private PurchaseOrderLine loadPurchaseOrderLine(Long id) {
        return purchaseOrderLineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrderLine", id));
    }

    private void applyForm(PurchaseOrderLine purchaseOrderLine, PurchaseOrderLineFormDto request) {

        purchaseOrderLine.setOrderId(request.getOrderId());
        purchaseOrderLine.setProductId(request.getProductId());
        purchaseOrderLine.setDescription(request.getDescription());
        purchaseOrderLine.setQuantity(request.getQuantity());
        purchaseOrderLine.setUnitPrice(request.getUnitPrice());
        purchaseOrderLine.setDiscountPercent(request.getDiscountPercent() == null ? BigDecimal.ZERO : request.getDiscountPercent());
        purchaseOrderLine.setTaxPercent(request.getTaxPercent() == null ? BigDecimal.ZERO : request.getTaxPercent());
        purchaseOrderLine.setLineTotal(request.getLineTotal());

    }

    private PurchaseOrderLineDisplayDto toDisplay(PurchaseOrderLine purchaseOrderLine) {
        return PurchaseOrderLineDisplayDto.builder()
                .id(purchaseOrderLine.getId())

                .orderId(purchaseOrderLine.getOrderId())
                .productId(purchaseOrderLine.getProductId())
                .description(purchaseOrderLine.getDescription())
                .quantity(purchaseOrderLine.getQuantity())
                .unitPrice(purchaseOrderLine.getUnitPrice())
                .discountPercent(purchaseOrderLine.getDiscountPercent())
                .taxPercent(purchaseOrderLine.getTaxPercent())
                .lineTotal(purchaseOrderLine.getLineTotal())

                .createdAt(purchaseOrderLine.getCreatedAt())
                .updatedAt(purchaseOrderLine.getUpdatedAt())
                .build();
    }

}
