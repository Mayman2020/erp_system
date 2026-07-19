package com.erp.system.ui.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ScreenSettingDto(
        String screenKey,
        boolean enabled,
        String updatedBy,
        Instant updatedAt
) {
}
