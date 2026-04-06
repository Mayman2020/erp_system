package com.erp.system.auth.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record AdminUserDto(
        Long id,
        String username,
        String email,
        String phone,
        String primaryRole,
        boolean active,
        String fullName,
        String fullNameEn,
        String fullNameAr,
        Instant createdAt,
        List<Long> roleIds,
        List<String> roleCodes
) {
}
