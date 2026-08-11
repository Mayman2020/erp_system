package com.erp.system.pmo.dto.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PmoRiskFormDto {
    @NotBlank
    private String title;
    @NotBlank
    private String severity;
    @NotBlank
    private String status;
    private String mitigation;
}
