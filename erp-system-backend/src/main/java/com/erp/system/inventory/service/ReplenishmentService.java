package com.erp.system.inventory.service;

import com.erp.system.common.exception.BusinessException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.purchases.repository.SupplierRepository;
import com.erp.system.inventory.domain.Product;
import com.erp.system.inventory.domain.ReplenishmentProposal;
import com.erp.system.inventory.domain.StockLevel;
import com.erp.system.inventory.domain.Warehouse;
import com.erp.system.inventory.dto.display.ReplenishmentProposalDisplayDto;
import com.erp.system.purchases.dto.display.PurchaseOrderDisplayDto;
import com.erp.system.purchases.dto.form.PurchaseOrderFormDto;
import com.erp.system.purchases.dto.form.PurchaseOrderLineInputDto;
import com.erp.system.purchases.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReplenishmentService {

    private static final String MODULE = "INVENTORY";
    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_CONVERTED = "CONVERTED";

    private final com.erp.system.inventory.repository.ReplenishmentProposalRepository proposalRepository;
    private final com.erp.system.inventory.repository.StockLevelRepository stockLevelRepository;
    private final ActivityLogService activityLogService;
    private final SupplierRepository supplierRepository;
    private final ObjectProvider<PurchaseOrderService> purchaseOrderServiceProvider;

    @Transactional(readOnly = true)
    public List<ReplenishmentProposalDisplayDto> getAll(String status) {
        List<ReplenishmentProposal> rows = status == null || status.isBlank()
                ? proposalRepository.findAllByOrderByIdDesc()
                : proposalRepository.findByStatusOrderByIdDesc(status.trim().toUpperCase(Locale.ROOT));
        return rows.stream().map(this::toDisplay).toList();
    }

    @Transactional
    public List<ReplenishmentProposalDisplayDto> generate() {
        List<StockLevel> lowLevels = stockLevelRepository.findLowStockLevels();
        List<ReplenishmentProposalDisplayDto> created = new ArrayList<>();
        for (StockLevel level : lowLevels) {
            Product product = level.getProduct();
            Warehouse warehouse = level.getWarehouse();
            if (product.getReorderLevel() == null || product.getReorderLevel().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal currentQty = level.getQuantity() == null ? BigDecimal.ZERO : level.getQuantity();
            if (currentQty.compareTo(product.getReorderLevel()) >= 0) {
                continue;
            }
            if (proposalRepository.findByWarehouseIdAndProductIdAndStatus(
                    warehouse.getId(), product.getId(), STATUS_OPEN).isPresent()) {
                continue;
            }
            BigDecimal proposedQty = product.getReorderLevel().subtract(currentQty).max(BigDecimal.ONE);
            ReplenishmentProposal proposal = ReplenishmentProposal.builder()
                    .warehouse(warehouse)
                    .product(product)
                    .currentQty(currentQty)
                    .reorderLevel(product.getReorderLevel())
                    .proposedQty(proposedQty)
                    .status(STATUS_OPEN)
                    .build();
            proposal = proposalRepository.save(proposal);
            created.add(toDisplay(proposal));
        }
        activityLogService.log(MODULE, "GENERATE", "REPLENISHMENT", null, null,
                "Generated " + created.size() + " replenishment proposals");
        return created;
    }

    @Transactional
    public PurchaseOrderDisplayDto convertToPurchaseOrder(Long warehouseId, Long supplierId) {
        PurchaseOrderService purchaseOrderService = purchaseOrderServiceProvider.getIfAvailable();
        if (purchaseOrderService == null) {
            throw new BusinessException("Purchase order service is not available");
        }
        if (supplierId == null) {
            throw new BusinessException("Supplier is required to create a purchase order");
        }
        if (!supplierRepository.existsById(supplierId)) {
            throw new BusinessException("Supplier not found: " + supplierId);
        }
        List<ReplenishmentProposal> open = proposalRepository.findByStatusOrderByIdDesc(STATUS_OPEN);
        Map<Long, List<ReplenishmentProposal>> byWarehouse = new LinkedHashMap<>();
        for (ReplenishmentProposal proposal : open) {
            Long whId = proposal.getWarehouse().getId();
            if (warehouseId != null && !warehouseId.equals(whId)) {
                continue;
            }
            byWarehouse.computeIfAbsent(whId, k -> new ArrayList<>()).add(proposal);
        }
        if (byWarehouse.isEmpty()) {
            throw new BusinessException("No open replenishment proposals to convert");
        }
        Long targetWarehouse = warehouseId != null ? warehouseId : byWarehouse.keySet().iterator().next();
        List<ReplenishmentProposal> batch = byWarehouse.get(targetWarehouse);
        if (batch == null || batch.isEmpty()) {
            throw new BusinessException("No open replenishment proposals for warehouse");
        }

        PurchaseOrderFormDto form = new PurchaseOrderFormDto();
        form.setOrderDate(LocalDate.now());
        form.setWarehouseId(targetWarehouse);
        form.setSupplierId(supplierId);
        form.setNotes("Auto-generated from replenishment proposals");
        form.setLines(batch.stream().map(p -> {
            PurchaseOrderLineInputDto line = new PurchaseOrderLineInputDto();
            line.setProductId(p.getProduct().getId());
            line.setQuantity(p.getProposedQty());
            line.setUnitPrice(p.getProduct().getCostPrice());
            line.setDiscountPercent(BigDecimal.ZERO);
            line.setTaxPercent(BigDecimal.ZERO);
            return line;
        }).toList());

        PurchaseOrderDisplayDto order = purchaseOrderService.create(form);
        for (ReplenishmentProposal proposal : batch) {
            proposal.setStatus(STATUS_CONVERTED);
            proposal.setPurchaseOrderId(order.getId());
            proposalRepository.save(proposal);
        }
        activityLogService.log(MODULE, "CONVERT", "REPLENISHMENT", order.getId(), order.getOrderNumber(),
                "Converted replenishment proposals to purchase order");
        return order;
    }

    private ReplenishmentProposalDisplayDto toDisplay(ReplenishmentProposal proposal) {
        Product product = proposal.getProduct();
        Warehouse warehouse = proposal.getWarehouse();
        return ReplenishmentProposalDisplayDto.builder()
                .id(proposal.getId())
                .warehouseId(warehouse.getId())
                .warehouseCode(warehouse.getCode())
                .warehouseName(resolveWarehouseName(warehouse))
                .productId(product.getId())
                .productCode(product.getCode())
                .productName(resolveProductName(product))
                .currentQty(proposal.getCurrentQty())
                .reorderLevel(proposal.getReorderLevel())
                .proposedQty(proposal.getProposedQty())
                .status(proposal.getStatus())
                .purchaseOrderId(proposal.getPurchaseOrderId())
                .createdAt(proposal.getCreatedAt())
                .updatedAt(proposal.getUpdatedAt())
                .build();
    }

    private String resolveProductName(Product product) {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale != null && "ar".equalsIgnoreCase(locale.getLanguage())
                && product.getNameAr() != null && !product.getNameAr().isBlank()) {
            return product.getNameAr();
        }
        return product.getNameEn();
    }

    private String resolveWarehouseName(Warehouse warehouse) {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale != null && "ar".equalsIgnoreCase(locale.getLanguage())
                && warehouse.getNameAr() != null && !warehouse.getNameAr().isBlank()) {
            return warehouse.getNameAr();
        }
        return warehouse.getNameEn();
    }
}
