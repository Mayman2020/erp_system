package com.erp.system.ui.dto;

import lombok.Builder;

@Builder
public record AdminMenuItemDto(
        String id,
        String parentId,
        Integer sortOrder,
        String itemType,
        String titleKey,
        String icon,
        String url
) {
}
