package com.erp.system.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUserActiveDto {

    @NotNull
    private Boolean active;
}
