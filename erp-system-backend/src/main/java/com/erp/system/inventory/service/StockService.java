package com.erp.system.inventory.service;

import com.erp.system.common.enums.StockMovementType;
import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.service.AccountingSettingsService;
import com.erp.system.common.service.NumberingService;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.inventory.domain.Product;
import com.erp.system.inventory.domain.StockLevel;
import com.erp.system.inventory.domain.StockMovement;
import com.erp.system.inventory.domain.Warehouse;
import com.erp.system.inventory.dto.display.LowStockAlertDisplayDto;
import com.erp.system.inventory.dto.display.StockLevelDisplayDto;
import com.erp.system.inventory.dto.display.StockMovementDisplayDto;
import com.erp.system.inventory.dto.form.StockMovementFormDto;
import com.erp.system.inventory.repository.ProductRepository;
import com.erp.system.inventory.repository.StockLevelRepository;
import com.erp.system.inventory.repository.StockMovementRepository;
import com.erp.system.inventory.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
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
public class StockService {

    private static final String MODULE = "INVENTORY";

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockMovementRepository stockMovementRepository;
    private final NumberingService numberingService;
    private final AccountingSettingsService accountingSettingsService;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<StockLevelDisplayDto> getStockLevels(Long productId, Long warehouseId) {
        List<StockLevel> levels;
        if (productId != null) {
            levels = stockLevelRepository.findByProductIdOrderByWarehouse_CodeAsc(productId);
        } else if (warehouseId != null) {
            levels = stockLevelRepository.findByWarehouseIdOrderByProduct_CodeAsc(warehouseId);
        } else {
            levels = stockLevelRepository.findAllByOrderByProduct_CodeAscWarehouse_CodeAsc();
        }
        return levels.stream().map(this::toLevelDisplay).toList();
    }

    @Transactional(readOnly = true)
    public StockLevelDisplayDto getStockLevel(Long id) {
        return toLevelDisplay(loadLevel(id));
    }

    @Transactional(readOnly = true)
    public List<LowStockAlertDisplayDto> getLowStockAlerts() {
        List<StockLevel> lowLevels = stockLevelRepository.findLowStockLevels();
        Map<Long, LowStockAlertDisplayDto> byProduct = new LinkedHashMap<>();
        for (StockLevel level : lowLevels) {
            Product product = level.getProduct();
            BigDecimal total = stockLevelRepository.sumQuantityByProductId(product.getId());
            byProduct.putIfAbsent(product.getId(), LowStockAlertDisplayDto.builder()
                    .productId(product.getId())
                    .productCode(product.getCode())
                    .productName(resolveProductName(product))
                    .reorderLevel(product.getReorderLevel())
                    .totalQuantity(total)
                    .shortfall(product.getReorderLevel().subtract(total).max(BigDecimal.ZERO))
                    .build());
        }
        return new ArrayList<>(byProduct.values());
    }

    @Transactional(readOnly = true)
    public List<StockMovementDisplayDto> getMovements(StockMovementType movementType, TransactionStatus status,
                                                      Long productId, Long warehouseId, String search,
                                                      LocalDate fromDate, LocalDate toDate) {
        List<StockMovement> movements = stockMovementRepository.findAllByOrderByMovementDateDescIdDesc();
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim().toLowerCase();
        return movements.stream()
                .filter(m -> movementType == null || m.getMovementType() == movementType)
                .filter(m -> status == null || m.getStatus() == status)
                .filter(m -> productId == null || m.getProduct().getId().equals(productId))
                .filter(m -> warehouseId == null || m.getWarehouse().getId().equals(warehouseId))
                .filter(m -> fromDate == null || !m.getMovementDate().isBefore(fromDate))
                .filter(m -> toDate == null || !m.getMovementDate().isAfter(toDate))
                .filter(m -> normalizedSearch == null
                        || m.getMovementNumber().toLowerCase().contains(normalizedSearch)
                        || m.getProduct().getCode().toLowerCase().contains(normalizedSearch))
                .map(this::toMovementDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public StockMovementDisplayDto getMovement(Long id) {
        return toMovementDisplay(loadMovement(id));
    }

    @Transactional
    public StockMovementDisplayDto createMovement(StockMovementFormDto request) {
        StockMovement movement = StockMovement.builder()
                .movementNumber(resolveMovementNumber(request.getMovementNumber()))
                .status(TransactionStatus.DRAFT)
                .build();
        applyForm(movement, request);
        movement = stockMovementRepository.save(movement);
        activityLogService.log(MODULE, "CREATE", "STOCK_MOVEMENT", movement.getId(), movement.getMovementNumber(),
                "Stock movement created");
        if (Boolean.TRUE.equals(request.getApproveImmediately())) {
            return approveMovement(movement.getId());
        }
        return toMovementDisplay(movement);
    }

    @Transactional
    public StockMovementDisplayDto updateMovement(Long id, StockMovementFormDto request) {
        StockMovement movement = loadMovement(id);
        if (movement.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft movements can be edited");
        }
        applyForm(movement, request);
        return toMovementDisplay(stockMovementRepository.save(movement));
    }

    @Transactional
    public StockMovementDisplayDto submitMovement(Long id) {
        StockMovement movement = loadMovement(id);
        if (movement.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft movements can be submitted");
        }
        movement.setStatus(TransactionStatus.PENDING);
        return toMovementDisplay(stockMovementRepository.save(movement));
    }

    @Transactional
    public StockMovementDisplayDto approveMovement(Long id) {
        StockMovement movement = loadMovement(id);
        if (movement.getStatus() == TransactionStatus.APPROVED) {
            return toMovementDisplay(movement);
        }
        if (movement.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Cancelled movements cannot be approved");
        }
        applyStockEffect(movement, false);
        movement.setStatus(TransactionStatus.APPROVED);
        movement = stockMovementRepository.save(movement);
        activityLogService.log(MODULE, "APPROVE", "STOCK_MOVEMENT", movement.getId(), movement.getMovementNumber(),
                "Stock movement approved");
        return toMovementDisplay(movement);
    }

    @Transactional
    public StockMovementDisplayDto cancelMovement(Long id) {
        StockMovement movement = loadMovement(id);
        if (movement.getStatus() == TransactionStatus.CANCELLED) {
            return toMovementDisplay(movement);
        }
        if (movement.getStatus() == TransactionStatus.APPROVED) {
            applyStockEffect(movement, true);
        }
        movement.setStatus(TransactionStatus.CANCELLED);
        movement = stockMovementRepository.save(movement);
        activityLogService.log(MODULE, "CANCEL", "STOCK_MOVEMENT", movement.getId(), movement.getMovementNumber(),
                "Stock movement cancelled");
        return toMovementDisplay(movement);
    }

    @Transactional
    public StockMovementDisplayDto stockIn(StockMovementFormDto request) {
        request.setMovementType(StockMovementType.IN);
        if (request.getApproveImmediately() == null) {
            request.setApproveImmediately(true);
        }
        return createMovement(request);
    }

    @Transactional
    public StockMovementDisplayDto stockOut(StockMovementFormDto request) {
        request.setMovementType(StockMovementType.OUT);
        if (request.getApproveImmediately() == null) {
            request.setApproveImmediately(true);
        }
        return createMovement(request);
    }

    @Transactional
    public StockMovementDisplayDto transferStock(StockMovementFormDto request) {
        request.setMovementType(StockMovementType.TRANSFER);
        if (request.getApproveImmediately() == null) {
            request.setApproveImmediately(true);
        }
        return createMovement(request);
    }

    @Transactional
    public void receiveStock(Long productId, Long warehouseId, BigDecimal quantity, BigDecimal unitCost,
                             String referenceType, Long referenceId, LocalDate movementDate) {
        StockMovementFormDto form = new StockMovementFormDto();
        form.setMovementDate(movementDate == null ? LocalDate.now() : movementDate);
        form.setMovementType(StockMovementType.IN);
        form.setProductId(productId);
        form.setWarehouseId(warehouseId);
        form.setQuantity(quantity);
        form.setUnitCost(unitCost);
        form.setReferenceType(referenceType);
        form.setReferenceId(referenceId);
        form.setApproveImmediately(true);
        stockIn(form);
    }

    private void applyForm(StockMovement movement, StockMovementFormDto request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.getWarehouseId()));
        Warehouse targetWarehouse = null;
        if (request.getTargetWarehouseId() != null) {
            targetWarehouse = warehouseRepository.findById(request.getTargetWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.getTargetWarehouseId()));
        }
        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantity must be greater than zero");
        }
        movement.setMovementDate(request.getMovementDate());
        movement.setMovementType(request.getMovementType());
        movement.setProduct(product);
        movement.setWarehouse(warehouse);
        movement.setTargetWarehouse(targetWarehouse);
        movement.setQuantity(request.getQuantity());
        movement.setUnitCost(request.getUnitCost() == null ? BigDecimal.ZERO : request.getUnitCost());
        movement.setReferenceType(request.getReferenceType());
        movement.setReferenceId(request.getReferenceId());
        movement.setNotes(request.getNotes());
    }

    private void applyStockEffect(StockMovement movement, boolean reverse) {
        int direction = reverse ? -1 : 1;
        switch (movement.getMovementType()) {
            case IN -> adjustStock(movement.getProduct(), movement.getWarehouse(),
                    movement.getQuantity().multiply(BigDecimal.valueOf(direction)));
            case OUT -> adjustStock(movement.getProduct(), movement.getWarehouse(),
                    movement.getQuantity().multiply(BigDecimal.valueOf(-direction)), true);
            case TRANSFER -> {
                if (movement.getTargetWarehouse() == null) {
                    throw new BusinessException("Transfer requires target warehouse");
                }
                adjustStock(movement.getProduct(), movement.getWarehouse(),
                        movement.getQuantity().multiply(BigDecimal.valueOf(-direction)), true);
                adjustStock(movement.getProduct(), movement.getTargetWarehouse(),
                        movement.getQuantity().multiply(BigDecimal.valueOf(direction)));
            }
            case ADJUSTMENT -> adjustStock(movement.getProduct(), movement.getWarehouse(),
                    movement.getQuantity().multiply(BigDecimal.valueOf(direction)));
            default -> throw new BusinessException("Unsupported movement type");
        }
    }

    private void adjustStock(Product product, Warehouse warehouse, BigDecimal delta) {
        adjustStock(product, warehouse, delta, false);
    }

    private void adjustStock(Product product, Warehouse warehouse, BigDecimal delta, boolean validateAvailability) {
        StockLevel level = stockLevelRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId())
                .orElseGet(() -> StockLevel.builder()
                        .product(product)
                        .warehouse(warehouse)
                        .quantity(BigDecimal.ZERO)
                        .reservedQuantity(BigDecimal.ZERO)
                        .build());
        BigDecimal newQty = level.getQuantity().add(delta);
        if (validateAvailability && newQty.compareTo(BigDecimal.ZERO) < 0) {
            boolean allowNegative = accountingSettingsService.getBooleanSetting("ALLOW_NEGATIVE_STOCK", false);
            if (!allowNegative) {
                throw new BusinessException("Insufficient stock for product " + product.getCode());
            }
        }
        level.setQuantity(newQty);
        stockLevelRepository.save(level);
    }

    private StockMovement loadMovement(Long id) {
        return stockMovementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockMovement", id));
    }

    private StockLevel loadLevel(Long id) {
        return stockLevelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockLevel", id));
    }

    private String resolveMovementNumber(String requested) {
        if (requested != null && !requested.isBlank()) {
            if (stockMovementRepository.existsByMovementNumberIgnoreCase(requested.trim())) {
                throw new BusinessException("Movement number already exists");
            }
            return requested.trim();
        }
        return generateMovementNumber();
    }

    private String generateMovementNumber() {
        try {
            return numberingService.generateNextNumber("STOCK_MOVEMENT");
        } catch (Exception exception) {
            return "SM-" + System.currentTimeMillis();
        }
    }

    private StockLevelDisplayDto toLevelDisplay(StockLevel level) {
        BigDecimal reserved = level.getReservedQuantity() == null ? BigDecimal.ZERO : level.getReservedQuantity();
        return StockLevelDisplayDto.builder()
                .id(level.getId())
                .productId(level.getProduct().getId())
                .productCode(level.getProduct().getCode())
                .productName(resolveProductName(level.getProduct()))
                .warehouseId(level.getWarehouse().getId())
                .warehouseCode(level.getWarehouse().getCode())
                .warehouseName(resolveWarehouseName(level.getWarehouse()))
                .quantity(level.getQuantity())
                .reservedQuantity(reserved)
                .availableQuantity(level.getQuantity().subtract(reserved))
                .createdAt(level.getCreatedAt())
                .updatedAt(level.getUpdatedAt())
                .build();
    }

    private StockMovementDisplayDto toMovementDisplay(StockMovement movement) {
        return StockMovementDisplayDto.builder()
                .id(movement.getId())
                .movementNumber(movement.getMovementNumber())
                .movementDate(movement.getMovementDate())
                .movementType(movement.getMovementType())
                .productId(movement.getProduct().getId())
                .productCode(movement.getProduct().getCode())
                .productName(resolveProductName(movement.getProduct()))
                .warehouseId(movement.getWarehouse().getId())
                .warehouseCode(movement.getWarehouse().getCode())
                .warehouseName(resolveWarehouseName(movement.getWarehouse()))
                .targetWarehouseId(movement.getTargetWarehouse() == null ? null : movement.getTargetWarehouse().getId())
                .targetWarehouseCode(movement.getTargetWarehouse() == null ? null : movement.getTargetWarehouse().getCode())
                .targetWarehouseName(movement.getTargetWarehouse() == null ? null : resolveWarehouseName(movement.getTargetWarehouse()))
                .quantity(movement.getQuantity())
                .unitCost(movement.getUnitCost())
                .referenceType(movement.getReferenceType())
                .referenceId(movement.getReferenceId())
                .notes(movement.getNotes())
                .status(movement.getStatus())
                .createdAt(movement.getCreatedAt())
                .updatedAt(movement.getUpdatedAt())
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
