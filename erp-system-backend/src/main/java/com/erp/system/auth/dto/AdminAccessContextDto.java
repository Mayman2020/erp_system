package com.erp.system.auth.dto;

import com.erp.system.ui.dto.AdminMenuItemDto;
import lombok.Builder;

import java.util.List;

@Builder
public record AdminAccessContextDto(
        List<AdminUserDto> users,
        List<AdminAccessRoleDto> roles,
        List<AdminMenuItemDto> menuItems
) {
}
