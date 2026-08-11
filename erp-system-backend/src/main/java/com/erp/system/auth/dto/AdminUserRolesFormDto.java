package com.erp.system.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminUserRolesFormDto {

    @NotBlank
    private String primaryRoleCode;

    private List<String> extraRoleCodes = new ArrayList<>();
}
