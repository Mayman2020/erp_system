package com.erp.system.purchases.service;

import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.purchases.domain.PurchaseReturnLine;
import com.erp.system.purchases.dto.display.PurchaseReturnLineDisplayDto;
import com.erp.system.purchases.dto.form.PurchaseReturnLineFormDto;
import com.erp.system.purchases.repository.PurchaseReturnLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PurchaseReturnLineService {

    private static final String MODULE = "PURCHASES";

    private final PurchaseReturnLineRepository purchaseReturnLineRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<PurchaseReturnLineDisplayDto> getAll() {
        return purchaseReturnLineRepository.findAllByOrderByIdDesc().stream()
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseReturnLineDisplayDto getById(Long id) {
        return toDisplay(loadPurchaseReturnLine(id));
    }

    @Transactional
    public PurchaseReturnLineDisplayDto create(PurchaseReturnLineFormDto request) {
        PurchaseReturnLine purchaseReturnLine = new PurchaseReturnLine();
        applyForm(purchaseReturnLine, request);
        purchaseReturnLine = purchaseReturnLineRepository.save(purchaseReturnLine);
        activityLogService.log(MODULE, "CREATE", "PurchaseReturnLine", purchaseReturnLine.getId(), String.valueOf(purchaseReturnLine.getId()),
                "Created PurchaseReturnLine " + purchaseReturnLine.getId());
        return toDisplay(purchaseReturnLine);
    }

    @Transactional
    public PurchaseReturnLineDisplayDto update(Long id, PurchaseReturnLineFormDto request) {
        PurchaseReturnLine purchaseReturnLine = loadPurchaseReturnLine(id);
        applyForm(purchaseReturnLine, request);
        purchaseReturnLine = purchaseReturnLineRepository.save(purchaseReturnLine);
        activityLogService.log(MODULE, "UPDATE", "PurchaseReturnLine", purchaseReturnLine.getId(), String.valueOf(purchaseReturnLine.getId()),
                "Updated PurchaseReturnLine " + purchaseReturnLine.getId());
        return toDisplay(purchaseReturnLine);
    }

    @Transactional
    public void delete(Long id) {
        PurchaseReturnLine purchaseReturnLine = loadPurchaseReturnLine(id);
        purchaseReturnLineRepository.delete(purchaseReturnLine);
        activityLogService.log(MODULE, "DELETE", "PurchaseReturnLine", id, String.valueOf(id),
                "Deleted PurchaseReturnLine " + id);
    }

    private PurchaseReturnLine loadPurchaseReturnLine(Long id) {
        return purchaseReturnLineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseReturnLine", id));
    }

    private void applyForm(PurchaseReturnLine purchaseReturnLine, PurchaseReturnLineFormDto request) {

        purchaseReturnLine.setReturnId(request.getReturnId());
        purchaseReturnLine.setProductId(request.getProductId());
        purchaseReturnLine.setQuantity(request.getQuantity());
        purchaseReturnLine.setUnitPrice(request.getUnitPrice());
        purchaseReturnLine.setLineTotal(request.getLineTotal());

    }

    private PurchaseReturnLineDisplayDto toDisplay(PurchaseReturnLine purchaseReturnLine) {
        return PurchaseReturnLineDisplayDto.builder()
                .id(purchaseReturnLine.getId())

                .returnId(purchaseReturnLine.getReturnId())
                .productId(purchaseReturnLine.getProductId())
                .quantity(purchaseReturnLine.getQuantity())
                .unitPrice(purchaseReturnLine.getUnitPrice())
                .lineTotal(purchaseReturnLine.getLineTotal())

                .createdAt(purchaseReturnLine.getCreatedAt())
                .updatedAt(purchaseReturnLine.getUpdatedAt())
                .build();
    }

}
