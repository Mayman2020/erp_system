package com.erp.system.inventory.service;

import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.inventory.domain.Warehouse;
import com.erp.system.inventory.dto.display.WarehouseDisplayDto;
import com.erp.system.inventory.dto.form.WarehouseFormDto;
import com.erp.system.inventory.repository.StockLevelRepository;
import com.erp.system.inventory.repository.StockMovementRepository;
import com.erp.system.inventory.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private static final String MODULE = "INVENTORY";

    private final WarehouseRepository warehouseRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<WarehouseDisplayDto> getWarehouses(Boolean active, String search) {
        List<Warehouse> warehouses = search != null && !search.isBlank()
                ? warehouseRepository.search(search.trim())
                : warehouseRepository.findAllByOrderByCodeAsc();

        return warehouses.stream()
                .filter(warehouse -> active == null || warehouse.isActive() == active)
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public WarehouseDisplayDto getWarehouse(Long id) {
        return toDisplay(loadWarehouse(id));
    }

    @Transactional
    public WarehouseDisplayDto createWarehouse(WarehouseFormDto request) {
        String code = request.getCode().trim();
        if (warehouseRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("Warehouse code already exists: " + code);
        }

        Warehouse warehouse = new Warehouse();
        applyForm(warehouse, request, code);
        warehouse = warehouseRepository.save(warehouse);

        activityLogService.log(MODULE, "CREATE", "Warehouse", warehouse.getId(), warehouse.getCode(),
                "Created warehouse " + warehouse.getCode());
        return toDisplay(warehouse);
    }

    @Transactional
    public WarehouseDisplayDto updateWarehouse(Long id, WarehouseFormDto request) {
        Warehouse warehouse = loadWarehouse(id);
        String code = request.getCode().trim();
        if (!code.equalsIgnoreCase(warehouse.getCode()) && warehouseRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new BusinessException("Warehouse code already exists: " + code);
        }

        applyForm(warehouse, request, code);
        warehouse = warehouseRepository.save(warehouse);

        activityLogService.log(MODULE, "UPDATE", "Warehouse", warehouse.getId(), warehouse.getCode(),
                "Updated warehouse " + warehouse.getCode());
        return toDisplay(warehouse);
    }

    @Transactional
    public void deactivateWarehouse(Long id) {
        Warehouse warehouse = loadWarehouse(id);
        warehouse.setActive(false);
        warehouseRepository.save(warehouse);
        activityLogService.log(MODULE, "DEACTIVATE", "Warehouse", warehouse.getId(), warehouse.getCode(),
                "Deactivated warehouse " + warehouse.getCode());
    }

    @Transactional
    public void activateWarehouse(Long id) {
        Warehouse warehouse = loadWarehouse(id);
        warehouse.setActive(true);
        warehouseRepository.save(warehouse);
        activityLogService.log(MODULE, "ACTIVATE", "Warehouse", warehouse.getId(), warehouse.getCode(),
                "Activated warehouse " + warehouse.getCode());
    }

    @Transactional
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = loadWarehouse(id);
        if (!stockLevelRepository.findByWarehouseIdOrderByProduct_CodeAsc(id).isEmpty()) {
            throw new BusinessException("Cannot delete warehouse that has stock levels");
        }
        if (!stockMovementRepository.findByWarehouseIdOrderByMovementDateDescIdDesc(id).isEmpty()) {
            throw new BusinessException("Cannot delete warehouse that has stock movements");
        }
        warehouseRepository.delete(warehouse);
        activityLogService.log(MODULE, "DELETE", "Warehouse", id, warehouse.getCode(),
                "Deleted warehouse " + warehouse.getCode());
    }

    private void applyForm(Warehouse warehouse, WarehouseFormDto request, String code) {
        warehouse.setCode(code);
        warehouse.setNameEn(request.getNameEn().trim());
        warehouse.setNameAr(normalizeOptional(request.getNameAr()));
        warehouse.setLocation(normalizeOptional(request.getLocation()));
        warehouse.setActive(request.getActive() == null || request.getActive());
    }

    Warehouse loadWarehouse(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private WarehouseDisplayDto toDisplay(Warehouse warehouse) {
        return WarehouseDisplayDto.builder()
                .id(warehouse.getId())
                .code(warehouse.getCode())
                .name(resolveLocalizedName(warehouse.getNameEn(), warehouse.getNameAr()))
                .nameEn(warehouse.getNameEn())
                .nameAr(warehouse.getNameAr())
                .location(warehouse.getLocation())
                .active(warehouse.isActive())
                .createdAt(warehouse.getCreatedAt())
                .updatedAt(warehouse.getUpdatedAt())
                .build();
    }

    private String resolveLocalizedName(String nameEn, String nameAr) {
        if ("ar".equalsIgnoreCase(LocaleContextHolder.getLocale().getLanguage()) && nameAr != null) {
            return nameAr;
        }
        return nameEn;
    }
}
