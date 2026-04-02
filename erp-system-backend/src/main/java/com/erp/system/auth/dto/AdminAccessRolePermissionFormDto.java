package com.erp.system.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminAccessRolePermissionFormDto {

    @NotBlank
    private String menuItemId;

    private Boolean canView = false;

    private Boolean canCreate = false;

    private Boolean canEdit = false;

    private Boolean canDelete = false;
}
