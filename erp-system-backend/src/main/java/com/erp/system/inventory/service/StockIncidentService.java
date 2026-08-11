package com.erp.system.inventory.service;

import com.erp.system.common.enums.StockMovementType;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.security.SecurityUtils;
import com.erp.system.common.service.NumberingService;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.inventory.domain.Product;
import com.erp.system.inventory.domain.StockIncident;
import com.erp.system.inventory.domain.Warehouse;
import com.erp.system.inventory.dto.display.StockIncidentDisplayDto;
import com.erp.system.inventory.dto.display.StockMovementDisplayDto;
import com.erp.system.inventory.dto.form.StockIncidentFormDto;
import com.erp.system.inventory.dto.form.StockMovementFormDto;
import com.erp.system.inventory.repository.ProductRepository;
import com.erp.system.inventory.repository.StockIncidentRepository;
import com.erp.system.inventory.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StockIncidentService {

    private static final String MODULE = "INVENTORY";
    private static final Set<String> OUT_TYPES = Set.of("DAMAGED", "LOST");

    private final StockIncidentRepository incidentRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockService stockService;
    private final NumberingService numberingService;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<StockIncidentDisplayDto> getAll(String status) {
        return incidentRepository.findAllByOrderByIdDesc().stream()
                .filter(i -> status == null || status.isBlank() || status.equalsIgnoreCase(i.getStatus()))
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public StockIncidentDisplayDto getById(Long id) {
        return toDisplay(loadIncident(id));
    }

    @Transactional
    public StockIncidentDisplayDto create(StockIncidentFormDto request) {
        StockIncident incident = StockIncident.builder()
                .incidentNo(resolveIncidentNo(request.getIncidentNo()))
                .status("DRAFT")
                .build();
        applyForm(incident, request);
        incident = incidentRepository.save(incident);
        activityLogService.log(MODULE, "CREATE", "STOCK_INCIDENT", incident.getId(), incident.getIncidentNo(),
                "Stock incident created");
        return toDisplay(incident);
    }

    @Transactional
    public StockIncidentDisplayDto update(Long id, StockIncidentFormDto request) {
        StockIncident incident = loadIncident(id);
        if (!"DRAFT".equalsIgnoreCase(incident.getStatus())) {
            throw new BusinessException("Only draft incidents can be edited");
        }
        applyForm(incident, request);
        return toDisplay(incidentRepository.save(incident));
    }

    @Transactional
    public StockIncidentDisplayDto approve(Long id) {
        StockIncident incident = loadIncident(id);
        if ("APPROVED".equalsIgnoreCase(incident.getStatus())) {
            return toDisplay(incident);
        }
        if (!"DRAFT".equalsIgnoreCase(incident.getStatus())) {
            throw new BusinessException("Only draft incidents can be approved");
        }
        BigDecimal unitCost = incident.getUnitCost() == null ? BigDecimal.ZERO : incident.getUnitCost();
        incident.setFinancialImpact(incident.getQuantity().multiply(unitCost));
        StockMovementDisplayDto movement = createStockMovement(incident, unitCost);
        incident.setMovementId(movement.getId());
        incident.setStatus("APPROVED");
        incident.setApprovedBy(SecurityUtils.currentUsername());
        incident.setApprovedAt(Instant.now());
        incident = incidentRepository.save(incident);
        activityLogService.log(MODULE, "APPROVE", "STOCK_INCIDENT", incident.getId(), incident.getIncidentNo(),
                "Stock incident approved");
        return toDisplay(incident);
    }

    @Transactional
    public void delete(Long id) {
        StockIncident incident = loadIncident(id);
        if (!"DRAFT".equalsIgnoreCase(incident.getStatus())) {
            throw new BusinessException("Only draft incidents can be deleted");
        }
        incidentRepository.delete(incident);
        activityLogService.log(MODULE, "DELETE", "STOCK_INCIDENT", id, incident.getIncidentNo(),
                "Stock incident deleted");
    }

    private StockMovementDisplayDto createStockMovement(StockIncident incident, BigDecimal unitCost) {
        StockMovementFormDto form = new StockMovementFormDto();
        form.setMovementDate(LocalDate.now());
        form.setProductId(incident.getProduct().getId());
        form.setWarehouseId(incident.getWarehouse().getId());
        form.setUnitCost(unitCost);
        form.setReferenceType("STOCK_INCIDENT");
        form.setReferenceId(incident.getId());
        form.setNotes(incident.getNotes());
        form.setApproveImmediately(true);
        if (OUT_TYPES.contains(incident.getIncidentType().toUpperCase(Locale.ROOT))) {
            form.setMovementType(StockMovementType.OUT);
            form.setQuantity(incident.getQuantity());
        } else {
            form.setMovementType(StockMovementType.ADJUSTMENT);
            form.setQuantity(incident.getQuantity().negate());
        }
        return stockService.createMovement(form);
    }

    private void applyForm(StockIncident incident, StockIncidentFormDto request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.getWarehouseId()));
        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantity must be greater than zero");
        }
        incident.setWarehouse(warehouse);
        incident.setProduct(product);
        incident.setQuantity(request.getQuantity());
        incident.setIncidentType(request.getIncidentType().trim().toUpperCase(Locale.ROOT));
        incident.setReasonCode(request.getReasonCode());
        incident.setNotes(request.getNotes());
        BigDecimal unitCost = request.getUnitCost();
        if (unitCost == null || unitCost.compareTo(BigDecimal.ZERO) == 0) {
            unitCost = product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO;
        }
        incident.setUnitCost(unitCost);
        incident.setFinancialImpact(request.getQuantity().multiply(unitCost));
    }

    private String resolveIncidentNo(String requested) {
        if (StringUtils.hasText(requested)) {
            String normalized = requested.trim();
            if (incidentRepository.existsByIncidentNoIgnoreCase(normalized)) {
                throw new BusinessException("Incident number already exists");
            }
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("STOCK_INCIDENT");
        } catch (Exception ex) {
            return "INC-" + System.currentTimeMillis();
        }
    }

    private StockIncident loadIncident(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockIncident", id));
    }

    private StockIncidentDisplayDto toDisplay(StockIncident incident) {
        Product product = incident.getProduct();
        Warehouse warehouse = incident.getWarehouse();
        return StockIncidentDisplayDto.builder()
                .id(incident.getId())
                .incidentNo(incident.getIncidentNo())
                .warehouseId(warehouse.getId())
                .warehouseCode(warehouse.getCode())
                .warehouseName(resolveWarehouseName(warehouse))
                .productId(product.getId())
                .productCode(product.getCode())
                .productName(resolveProductName(product))
                .quantity(incident.getQuantity())
                .incidentType(incident.getIncidentType())
                .reasonCode(incident.getReasonCode())
                .notes(incident.getNotes())
                .unitCost(incident.getUnitCost())
                .financialImpact(incident.getFinancialImpact())
                .status(incident.getStatus())
                .approvedBy(incident.getApprovedBy())
                .approvedAt(incident.getApprovedAt())
                .movementId(incident.getMovementId())
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .createdBy(incident.getCreatedBy())
                .updatedBy(incident.getUpdatedBy())
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
