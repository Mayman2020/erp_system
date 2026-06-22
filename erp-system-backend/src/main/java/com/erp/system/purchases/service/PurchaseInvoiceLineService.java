package com.erp.system.purchases.service;

import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.purchases.domain.PurchaseInvoiceLine;
import com.erp.system.purchases.dto.display.PurchaseInvoiceLineDisplayDto;
import com.erp.system.purchases.dto.form.PurchaseInvoiceLineFormDto;
import com.erp.system.purchases.repository.PurchaseInvoiceLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PurchaseInvoiceLineService {

    private static final String MODULE = "PURCHASES";

    private final PurchaseInvoiceLineRepository purchaseInvoiceLineRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<PurchaseInvoiceLineDisplayDto> getAll() {
        return purchaseInvoiceLineRepository.findAllByOrderByIdDesc().stream()
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseInvoiceLineDisplayDto getById(Long id) {
        return toDisplay(loadPurchaseInvoiceLine(id));
    }

    @Transactional
    public PurchaseInvoiceLineDisplayDto create(PurchaseInvoiceLineFormDto request) {
        PurchaseInvoiceLine purchaseInvoiceLine = new PurchaseInvoiceLine();
        applyForm(purchaseInvoiceLine, request);
        purchaseInvoiceLine = purchaseInvoiceLineRepository.save(purchaseInvoiceLine);
        activityLogService.log(MODULE, "CREATE", "PurchaseInvoiceLine", purchaseInvoiceLine.getId(), String.valueOf(purchaseInvoiceLine.getId()),
                "Created PurchaseInvoiceLine " + purchaseInvoiceLine.getId());
        return toDisplay(purchaseInvoiceLine);
    }

    @Transactional
    public PurchaseInvoiceLineDisplayDto update(Long id, PurchaseInvoiceLineFormDto request) {
        PurchaseInvoiceLine purchaseInvoiceLine = loadPurchaseInvoiceLine(id);
        applyForm(purchaseInvoiceLine, request);
        purchaseInvoiceLine = purchaseInvoiceLineRepository.save(purchaseInvoiceLine);
        activityLogService.log(MODULE, "UPDATE", "PurchaseInvoiceLine", purchaseInvoiceLine.getId(), String.valueOf(purchaseInvoiceLine.getId()),
                "Updated PurchaseInvoiceLine " + purchaseInvoiceLine.getId());
        return toDisplay(purchaseInvoiceLine);
    }

    @Transactional
    public void delete(Long id) {
        PurchaseInvoiceLine purchaseInvoiceLine = loadPurchaseInvoiceLine(id);
        purchaseInvoiceLineRepository.delete(purchaseInvoiceLine);
        activityLogService.log(MODULE, "DELETE", "PurchaseInvoiceLine", id, String.valueOf(id),
                "Deleted PurchaseInvoiceLine " + id);
    }

    private PurchaseInvoiceLine loadPurchaseInvoiceLine(Long id) {
        return purchaseInvoiceLineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseInvoiceLine", id));
    }

    private void applyForm(PurchaseInvoiceLine purchaseInvoiceLine, PurchaseInvoiceLineFormDto request) {

        purchaseInvoiceLine.setInvoiceId(request.getInvoiceId());
        purchaseInvoiceLine.setProductId(request.getProductId());
        purchaseInvoiceLine.setDescription(request.getDescription());
        purchaseInvoiceLine.setQuantity(request.getQuantity());
        purchaseInvoiceLine.setUnitPrice(request.getUnitPrice());
        purchaseInvoiceLine.setDiscountPercent(request.getDiscountPercent() == null ? BigDecimal.ZERO : request.getDiscountPercent());
        purchaseInvoiceLine.setTaxPercent(request.getTaxPercent() == null ? BigDecimal.ZERO : request.getTaxPercent());
        purchaseInvoiceLine.setLineTotal(request.getLineTotal());

    }

    private PurchaseInvoiceLineDisplayDto toDisplay(PurchaseInvoiceLine purchaseInvoiceLine) {
        return PurchaseInvoiceLineDisplayDto.builder()
                .id(purchaseInvoiceLine.getId())

                .invoiceId(purchaseInvoiceLine.getInvoiceId())
                .productId(purchaseInvoiceLine.getProductId())
                .description(purchaseInvoiceLine.getDescription())
                .quantity(purchaseInvoiceLine.getQuantity())
                .unitPrice(purchaseInvoiceLine.getUnitPrice())
                .discountPercent(purchaseInvoiceLine.getDiscountPercent())
                .taxPercent(purchaseInvoiceLine.getTaxPercent())
                .lineTotal(purchaseInvoiceLine.getLineTotal())

                .createdAt(purchaseInvoiceLine.getCreatedAt())
                .updatedAt(purchaseInvoiceLine.getUpdatedAt())
                .build();
    }

}
