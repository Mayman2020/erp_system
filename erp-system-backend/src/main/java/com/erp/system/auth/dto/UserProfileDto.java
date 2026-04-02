package com.erp.system.auth.dto;

import lombok.Builder;

@Builder
public record UserProfileDto(
        Long id,
        Long userId,
        String fullName,
        String profileImage,
        String nationalId,
        String companyName
) {
}
