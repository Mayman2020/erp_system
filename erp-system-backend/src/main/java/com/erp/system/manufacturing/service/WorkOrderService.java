package com.erp.system.manufacturing.service;

import com.erp.system.common.enums.StockMovementType;
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
import com.erp.system.manufacturing.domain.ProductBomLine;
import com.erp.system.manufacturing.domain.WorkOrder;
import com.erp.system.manufacturing.dto.display.WorkOrderDisplayDto;
import com.erp.system.manufacturing.dto.form.WorkOrderFormDto;
import com.erp.system.manufacturing.repository.ProductBomLineRepository;
import com.erp.system.manufacturing.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkOrderService {

    private static final String MODULE = "MANUFACTURING";

    private final WorkOrderRepository workOrderRepository;
    private final ProductBomLineRepository productBomLineRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockService stockService;
    private final NumberingService numberingService;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<WorkOrderDisplayDto> getAll() {
        return workOrderRepository.findAllByOrderByPlannedStartDescIdDesc().stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public WorkOrderDisplayDto getById(Long id) {
        return toDisplay(load(id));
    }

    @Transactional
    public WorkOrderDisplayDto create(WorkOrderFormDto request) {
        WorkOrder order = new WorkOrder();
        applyForm(order, request);
        order.setOrderNumber(resolveNumber(request.getOrderNumber()));
        order.setStatus("PLANNED");
        order.setProducedQuantity(BigDecimal.ZERO);
        order = workOrderRepository.save(order);
        activityLogService.log(MODULE, "CREATE", "WorkOrder", order.getId(), order.getOrderNumber(),
                "Created work order " + order.getOrderNumber());
        return toDisplay(order);
    }

    @Transactional
    public WorkOrderDisplayDto update(Long id, WorkOrderFormDto request) {
        WorkOrder order = load(id);
        if (!"PLANNED".equals(order.getStatus())) {
            throw new BusinessException("Only planned work orders can be edited");
        }
        applyForm(order, request);
        if (request.getOrderNumber() != null && !request.getOrderNumber().isBlank()) {
            order.setOrderNumber(request.getOrderNumber().trim());
        }
        order = workOrderRepository.save(order);
        activityLogService.log(MODULE, "UPDATE", "WorkOrder", order.getId(), order.getOrderNumber(),
                "Updated work order " + order.getOrderNumber());
        return toDisplay(order);
    }

    @Transactional
    public void delete(Long id) {
        WorkOrder order = load(id);
        if (!"PLANNED".equals(order.getStatus()) && !"CANCELLED".equals(order.getStatus())) {
            throw new BusinessException("Only planned or cancelled work orders can be deleted");
        }
        workOrderRepository.delete(order);
        activityLogService.log(MODULE, "DELETE", "WorkOrder", id, order.getOrderNumber(),
                "Deleted work order " + order.getOrderNumber());
    }

    @Transactional
    public WorkOrderDisplayDto start(Long id, String actor) {
        WorkOrder order = load(id);
        if (!"PLANNED".equals(order.getStatus())) {
            throw new BusinessException("Only planned work orders can be started");
        }
        order.setStatus("IN_PROGRESS");
        order.setStartedAt(LocalDateTime.now());
        order.setUpdatedBy(actor);
        order = workOrderRepository.save(order);
        activityLogService.log(MODULE, "START", "WorkOrder", order.getId(), order.getOrderNumber(),
                "Started work order " + order.getOrderNumber());
        return toDisplay(order);
    }

    @Transactional
    public WorkOrderDisplayDto complete(Long id, String actor) {
        WorkOrder order = load(id);
        if (!"IN_PROGRESS".equals(order.getStatus()) && !"PLANNED".equals(order.getStatus())) {
            throw new BusinessException("Work order cannot be completed from status " + order.getStatus());
        }
        if (order.getWarehouseId() == null) {
            throw new BusinessException("Warehouse is required to complete production");
        }
        Long productId = order.getProductId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        List<ProductBomLine> bomLines = productBomLineRepository.findByParentProductIdOrderByIdAsc(productId);
        BigDecimal rolledUpCost = BigDecimal.ZERO;
        java.time.LocalDate movementDate = order.getPlannedEnd() != null ? order.getPlannedEnd() : java.time.LocalDate.now();
        for (ProductBomLine bomLine : bomLines) {
            Product component = productRepository.findById(bomLine.getComponentProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", bomLine.getComponentProductId()));
            BigDecimal requiredQty = bomLine.getQuantityPerUnit()
                    .multiply(order.getQuantity())
                    .setScale(4, RoundingMode.HALF_UP);
            BigDecimal componentCost = component.getCostPrice() == null ? BigDecimal.ZERO : component.getCostPrice();
            rolledUpCost = rolledUpCost.add(componentCost.multiply(requiredQty));

            StockMovementFormDto stockOut = new StockMovementFormDto();
            stockOut.setMovementDate(movementDate);
            stockOut.setMovementType(StockMovementType.OUT);
            stockOut.setProductId(component.getId());
            stockOut.setWarehouseId(order.getWarehouseId());
            stockOut.setQuantity(requiredQty);
            stockOut.setUnitCost(componentCost);
            stockOut.setReferenceType("WORK_ORDER");
            stockOut.setReferenceId(order.getId());
            stockOut.setNotes("Material consumption for " + order.getOrderNumber());
            stockOut.setApproveImmediately(true);
            stockService.stockOut(stockOut);
        }

        BigDecimal finishedUnitCost = bomLines.isEmpty()
                ? (product.getCostPrice() == null ? BigDecimal.ZERO : product.getCostPrice())
                : rolledUpCost.divide(order.getQuantity(), 4, RoundingMode.HALF_UP);
        stockService.receiveStock(
                product.getId(),
                order.getWarehouseId(),
                order.getQuantity(),
                finishedUnitCost,
                "WORK_ORDER",
                order.getId(),
                movementDate
        );
        order.setStatus("COMPLETED");
        order.setProducedQuantity(order.getQuantity());
        order.setCompletedAt(LocalDateTime.now());
        order.setUpdatedBy(actor);
        order = workOrderRepository.save(order);
        activityLogService.log(MODULE, "COMPLETE", "WorkOrder", order.getId(), order.getOrderNumber(),
                "Completed work order " + order.getOrderNumber());
        return toDisplay(order);
    }

    @Transactional
    public WorkOrderDisplayDto cancel(Long id, String actor) {
        WorkOrder order = load(id);
        if ("COMPLETED".equals(order.getStatus())) {
            throw new BusinessException("Completed work orders cannot be cancelled");
        }
        order.setStatus("CANCELLED");
        order.setUpdatedBy(actor);
        order = workOrderRepository.save(order);
        activityLogService.log(MODULE, "CANCEL", "WorkOrder", order.getId(), order.getOrderNumber(),
                "Cancelled work order " + order.getOrderNumber());
        return toDisplay(order);
    }

    private void applyForm(WorkOrder order, WorkOrderFormDto request) {
        if (!productRepository.existsById(request.getProductId())) {
            throw new BusinessException("Product not found");
        }
        if (request.getWarehouseId() != null && !warehouseRepository.existsById(request.getWarehouseId())) {
            throw new BusinessException("Warehouse not found");
        }
        order.setProductId(request.getProductId());
        order.setWarehouseId(request.getWarehouseId());
        order.setQuantity(request.getQuantity());
        order.setPlannedStart(request.getPlannedStart());
        order.setPlannedEnd(request.getPlannedEnd());
        order.setNotes(request.getNotes());
    }

    private String resolveNumber(String requested) {
        String normalized = requested == null ? null : requested.trim();
        if (normalized != null && !normalized.isEmpty()) {
            if (workOrderRepository.existsByOrderNumberIgnoreCase(normalized)) {
                throw new BusinessException("Work order number already exists");
            }
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("WORK_ORDER");
        } catch (Exception ex) {
            return "WO-" + System.currentTimeMillis();
        }
    }

    private WorkOrder load(Long id) {
        return workOrderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("WorkOrder", id));
    }

    private WorkOrderDisplayDto toDisplay(WorkOrder order) {
        Product product = productRepository.findById(order.getProductId()).orElse(null);
        Warehouse warehouse = order.getWarehouseId() == null ? null : warehouseRepository.findById(order.getWarehouseId()).orElse(null);
        return WorkOrderDisplayDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .productId(order.getProductId())
                .productCode(product != null ? product.getCode() : null)
                .productName(product != null ? product.getNameEn() : null)
                .warehouseId(order.getWarehouseId())
                .warehouseName(warehouse != null ? warehouse.getNameEn() : null)
                .quantity(order.getQuantity())
                .producedQuantity(order.getProducedQuantity())
                .status(order.getStatus())
                .plannedStart(order.getPlannedStart())
                .plannedEnd(order.getPlannedEnd())
                .notes(order.getNotes())
                .startedAt(order.getStartedAt())
                .completedAt(order.getCompletedAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
