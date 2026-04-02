package com.erp.system.auth.dto;

import lombok.Builder;

@Builder
public record AdminAccessRolePermissionDto(
        String menuItemId,
        String titleKey,
        String url,
        String itemType,
        String parentId,
        Integer sortOrder,
        boolean canView,
        boolean canCreate,
        boolean canEdit,
        boolean canDelete
) {
}
