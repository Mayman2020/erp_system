package com.erp.system.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminAccessRoleFormDto {

    @NotBlank
    @Size(max = 60)
    private String code;

    @NotBlank
    @Size(max = 150)
    private String nameEn;

    @NotBlank
    @Size(max = 150)
    private String nameAr;

    private Boolean active = true;

    @NotEmpty
    private List<AdminAccessRolePermissionFormDto> permissions = new ArrayList<>();
}
