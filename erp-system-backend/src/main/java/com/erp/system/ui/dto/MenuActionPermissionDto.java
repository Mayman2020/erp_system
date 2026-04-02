package com.erp.system.ui.dto;

import lombok.Builder;

@Builder
public record MenuActionPermissionDto(
        String menuItemId,
        boolean canView,
        boolean canCreate,
        boolean canEdit,
        boolean canDelete
) {
}
