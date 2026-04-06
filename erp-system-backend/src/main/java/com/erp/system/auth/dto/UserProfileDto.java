package com.erp.system.auth.dto;

import lombok.Builder;

@Builder
public record UserProfileDto(
        Long id,
        Long userId,
        String fullName,
        String fullNameEn,
        String fullNameAr,
        String profileImage,
        String nationalId,
        String companyName,
        String companyNameEn,
        String companyNameAr
) {
}
