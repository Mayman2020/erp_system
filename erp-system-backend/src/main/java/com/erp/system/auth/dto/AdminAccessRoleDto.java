package com.erp.system.auth.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AdminAccessRoleDto(
        Long id,
        String code,
        String nameEn,
        String nameAr,
        boolean active,
        boolean systemRole,
        List<AdminAccessRolePermissionDto> permissions
) {
}
