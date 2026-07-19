package com.erp.system.ui.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScreenSettingFormDto {

    @NotNull
    private Boolean enabled;
}
