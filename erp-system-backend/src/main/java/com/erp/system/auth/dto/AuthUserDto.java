package com.erp.system.auth.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record AuthUserDto(
        Long id,
        String username,
        String email,
        String phone,
        String role,
        List<String> roles,
        boolean active,
        Instant createdAt,
        UserProfileDto profile
) {
}
