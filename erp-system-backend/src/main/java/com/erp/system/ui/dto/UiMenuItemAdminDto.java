package com.erp.system.ui.dto;

import lombok.Builder;

@Builder
public record UiMenuItemAdminDto(
        String id,
        String parentId,
        Integer sortOrder,
        String itemType,
        String titleKey,
        String icon,
        String url,
        Boolean external,
        Boolean targetBlank,
        String rolesCsv,
        String itemClasses,
        Boolean breadcrumbsFlag
) {
}
