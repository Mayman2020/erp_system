package com.erp.system.common.service;

import com.erp.system.common.dto.AdminLookupTypeDto;
import com.erp.system.common.dto.AdminLookupTypeFormDto;
import com.erp.system.common.dto.AdminLookupValueDto;
import com.erp.system.common.dto.AdminLookupValueFormDto;
import com.erp.system.common.entity.LookupType;
import com.erp.system.common.entity.LookupValue;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.repository.LookupTypeRepository;
import com.erp.system.common.repository.LookupValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminLookupService {

    private final LookupTypeRepository lookupTypeRepository;
    private final LookupValueRepository lookupValueRepository;

    @Transactional(readOnly = true)
    public List<AdminLookupTypeDto> getLookupTypes() {
        return lookupTypeRepository.findAllByOrderBySortOrderAscCodeAsc().stream()
                .map(this::toTypeDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminLookupValueDto> getLookupValues(String typeCode) {
        return lookupValueRepository.findByTypeCodeIgnoreCaseOrderBySortOrderAscCodeAsc(typeCode).stream()
                .map(this::toValueDto)
                .toList();
    }

    @Transactional
    public AdminLookupTypeDto createLookupType(AdminLookupTypeFormDto request) {
        String code = normalizeCode(request.getCode());
        if (lookupTypeRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("Lookup type code already exists");
        }

        LookupType type = LookupType.builder()
                .code(code)
                .nameEn(normalize(request.getNameEn()))
                .nameAr(normalize(request.getNameAr()))
                .sortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .active(Boolean.TRUE.equals(request.getActive()))
                .build();
        return toTypeDto(lookupTypeRepository.save(type));
    }

    @Transactional
    public AdminLookupTypeDto updateLookupType(Long typeId, AdminLookupTypeFormDto request) {
        LookupType type = lookupTypeRepository.findById(typeId)
                .orElseThrow(() -> new ResourceNotFoundException("LookupType", typeId));

        String code = normalizeCode(request.getCode());
        if (lookupTypeRepository.existsByCodeIgnoreCaseAndIdNot(code, typeId)) {
            throw new BusinessException("Lookup type code already exists");
        }

        String oldCode = type.getCode();
        type.setCode(code);
        type.setNameEn(normalize(request.getNameEn()));
        type.setNameAr(normalize(request.getNameAr()));
        type.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        type.setActive(Boolean.TRUE.equals(request.getActive()));
        LookupType saved = lookupTypeRepository.save(type);

        if (!oldCode.equalsIgnoreCase(code)) {
            List<LookupValue> values = lookupValueRepository.findByTypeCodeIgnoreCaseOrderBySortOrderAscCodeAsc(oldCode);
            values.forEach(value -> value.setTypeCode(code));
            lookupValueRepository.saveAll(values);
        }

        return toTypeDto(saved);
    }

    @Transactional
    public void deleteLookupType(Long typeId) {
        LookupType type = lookupTypeRepository.findById(typeId)
                .orElseThrow(() -> new ResourceNotFoundException("LookupType", typeId));
        List<LookupValue> values = lookupValueRepository.findByTypeCodeIgnoreCaseOrderBySortOrderAscCodeAsc(type.getCode());
        if (!values.isEmpty()) {
            throw new BusinessException("Cannot delete lookup type that still contains values");
        }
        lookupTypeRepository.delete(type);
    }

    @Transactional
    public AdminLookupValueDto createLookupValue(AdminLookupValueFormDto request) {
        String typeCode = normalizeTypeCode(request.getTypeCode());
        ensureLookupTypeExists(typeCode);
        String code = normalizeValueCode(request.getCode());
        if (lookupValueRepository.existsByTypeCodeIgnoreCaseAndCodeIgnoreCase(typeCode, code)) {
            throw new BusinessException("Lookup value code already exists in this type");
        }

        LookupValue value = LookupValue.builder()
                .typeCode(typeCode)
                .code(code)
                .nameEn(normalize(request.getNameEn()))
                .nameAr(normalize(request.getNameAr()))
                .sortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .active(Boolean.TRUE.equals(request.getActive()))
                .build();
        return toValueDto(lookupValueRepository.save(value));
    }

    @Transactional
    public AdminLookupValueDto updateLookupValue(Long valueId, AdminLookupValueFormDto request) {
        LookupValue value = lookupValueRepository.findById(valueId)
                .orElseThrow(() -> new ResourceNotFoundException("LookupValue", valueId));

        String typeCode = normalizeTypeCode(request.getTypeCode());
        ensureLookupTypeExists(typeCode);
        String code = normalizeValueCode(request.getCode());
        if (lookupValueRepository.existsByTypeCodeIgnoreCaseAndCodeIgnoreCaseAndIdNot(typeCode, code, valueId)) {
            throw new BusinessException("Lookup value code already exists in this type");
        }

        value.setTypeCode(typeCode);
        value.setCode(code);
        value.setNameEn(normalize(request.getNameEn()));
        value.setNameAr(normalize(request.getNameAr()));
        value.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        value.setActive(Boolean.TRUE.equals(request.getActive()));
        return toValueDto(lookupValueRepository.save(value));
    }

    @Transactional
    public void deleteLookupValue(Long valueId) {
        LookupValue value = lookupValueRepository.findById(valueId)
                .orElseThrow(() -> new ResourceNotFoundException("LookupValue", valueId));
        lookupValueRepository.delete(value);
    }

    private void ensureLookupTypeExists(String typeCode) {
        lookupTypeRepository.findByCodeIgnoreCase(typeCode)
                .orElseThrow(() -> new BusinessException("Lookup type does not exist"));
    }

    private AdminLookupTypeDto toTypeDto(LookupType type) {
        return AdminLookupTypeDto.builder()
                .id(type.getId())
                .code(type.getCode())
                .nameEn(type.getNameEn())
                .nameAr(type.getNameAr())
                .sortOrder(type.getSortOrder())
                .active(type.isActive())
                .build();
    }

    private AdminLookupValueDto toValueDto(LookupValue value) {
        return AdminLookupValueDto.builder()
                .id(value.getId())
                .typeCode(value.getTypeCode())
                .code(value.getCode())
                .nameEn(value.getNameEn())
                .nameAr(value.getNameAr())
                .sortOrder(value.getSortOrder())
                .active(value.isActive())
                .build();
    }

    private String normalize(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) {
            throw new BusinessException("AUTH.ERRORS.INVALID_REQUEST");
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        String normalized = value == null ? null : value.trim();
        return normalized == null || normalized.isBlank() ? null : normalized;
    }

    private String normalizeCode(String value) {
        return normalize(value).toLowerCase(Locale.ROOT);
    }

    private String normalizeTypeCode(String value) {
        return normalizeCode(value);
    }

    private String normalizeValueCode(String value) {
        return normalize(value).trim().toUpperCase(Locale.ROOT);
    }

}
