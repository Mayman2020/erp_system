package com.erp.system.auth.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record AuthUserDto(
        Long id,
        String username,
        String email,
        String phone,
        String role,
        boolean active,
        Instant createdAt,
        UserProfileDto profile
) {
}
