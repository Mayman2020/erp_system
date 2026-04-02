package com.erp.system.common.dto;

import lombok.Builder;

@Builder
public record AdminLookupValueDto(
        Long id,
        String typeCode,
        String code,
        String nameEn,
        String nameAr,
        Integer sortOrder,
        boolean active
) {
}
