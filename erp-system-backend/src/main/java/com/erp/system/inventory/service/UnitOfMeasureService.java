package com.erp.system.inventory.service;

import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.inventory.domain.UnitOfMeasure;
import com.erp.system.inventory.dto.display.UnitOfMeasureDisplayDto;
import com.erp.system.inventory.dto.form.UnitOfMeasureFormDto;
import com.erp.system.inventory.repository.ProductRepository;
import com.erp.system.inventory.repository.UnitOfMeasureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitOfMeasureService {

    private static final String MODULE = "INVENTORY";

    private final UnitOfMeasureRepository unitRepository;
    private final ProductRepository productRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<UnitOfMeasureDisplayDto> getUnits(Boolean active, String search) {
        List<UnitOfMeasure> units = search != null && !search.isBlank()
                ? unitRepository.search(search.trim())
                : unitRepository.findAllByOrderByCodeAsc();

        return units.stream()
                .filter(unit -> active == null || unit.isActive() == active)
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public UnitOfMeasureDisplayDto getUnit(Long id) {
        return toDisplay(loadUnit(id));
    }

    @Transactional
    public UnitOfMeasureDisplayDto createUnit(UnitOfMeasureFormDto request) {
        String code = request.getCode().trim();
        if (unitRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("Unit of measure code already exists: " + code);
        }

        UnitOfMeasure unit = new UnitOfMeasure();
        applyForm(unit, request, code);
        unit = unitRepository.save(unit);

        activityLogService.log(MODULE, "CREATE", "UnitOfMeasure", unit.getId(), unit.getCode(),
                "Created unit of measure " + unit.getCode());
        return toDisplay(unit);
    }

    @Transactional
    public UnitOfMeasureDisplayDto updateUnit(Long id, UnitOfMeasureFormDto request) {
        UnitOfMeasure unit = loadUnit(id);
        String code = request.getCode().trim();
        if (!code.equalsIgnoreCase(unit.getCode()) && unitRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new BusinessException("Unit of measure code already exists: " + code);
        }

        applyForm(unit, request, code);
        unit = unitRepository.save(unit);

        activityLogService.log(MODULE, "UPDATE", "UnitOfMeasure", unit.getId(), unit.getCode(),
                "Updated unit of measure " + unit.getCode());
        return toDisplay(unit);
    }

    @Transactional
    public void deactivateUnit(Long id) {
        UnitOfMeasure unit = loadUnit(id);
        unit.setActive(false);
        unitRepository.save(unit);
        activityLogService.log(MODULE, "DEACTIVATE", "UnitOfMeasure", unit.getId(), unit.getCode(),
                "Deactivated unit of measure " + unit.getCode());
    }

    @Transactional
    public void activateUnit(Long id) {
        UnitOfMeasure unit = loadUnit(id);
        unit.setActive(true);
        unitRepository.save(unit);
        activityLogService.log(MODULE, "ACTIVATE", "UnitOfMeasure", unit.getId(), unit.getCode(),
                "Activated unit of measure " + unit.getCode());
    }

    @Transactional
    public void deleteUnit(Long id) {
        UnitOfMeasure unit = loadUnit(id);
        if (productRepository.existsByUnit_Id(id)) {
            throw new BusinessException("Cannot delete unit of measure that is used by products");
        }
        unitRepository.delete(unit);
        activityLogService.log(MODULE, "DELETE", "UnitOfMeasure", id, unit.getCode(),
                "Deleted unit of measure " + unit.getCode());
    }

    private void applyForm(UnitOfMeasure unit, UnitOfMeasureFormDto request, String code) {
        unit.setCode(code);
        unit.setNameEn(request.getNameEn().trim());
        unit.setNameAr(normalizeOptional(request.getNameAr()));
        unit.setActive(request.getActive() == null || request.getActive());
    }

    UnitOfMeasure loadUnit(Long id) {
        return unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UnitOfMeasure", id));
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private UnitOfMeasureDisplayDto toDisplay(UnitOfMeasure unit) {
        return UnitOfMeasureDisplayDto.builder()
                .id(unit.getId())
                .code(unit.getCode())
                .name(resolveLocalizedName(unit.getNameEn(), unit.getNameAr()))
                .nameEn(unit.getNameEn())
                .nameAr(unit.getNameAr())
                .active(unit.isActive())
                .createdAt(unit.getCreatedAt())
                .updatedAt(unit.getUpdatedAt())
                .build();
    }

    private String resolveLocalizedName(String nameEn, String nameAr) {
        if ("ar".equalsIgnoreCase(LocaleContextHolder.getLocale().getLanguage()) && nameAr != null) {
            return nameAr;
        }
        return nameEn;
    }
}
