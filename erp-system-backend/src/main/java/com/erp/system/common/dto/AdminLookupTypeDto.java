package com.erp.system.common.dto;

import lombok.Builder;

@Builder
public record AdminLookupTypeDto(
        Long id,
        String code,
        String nameEn,
        String nameAr,
        Integer sortOrder,
        boolean active
) {
}
