package com.erp.system.auth.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AuthResponseDto(
        String token,
        String refreshToken,
        String type,
        List<String> roles,
        String userType,
        String displayName,
        long expiresInSeconds,
        long refreshExpiresInSeconds,
        AuthUserDto user
) {
}
