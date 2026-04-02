package com.erp.system.accounting.dto.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FiscalPeriodFormDto {

    @NotBlank(message = "VALIDATION.REQUIRED")
    private String periodName;

    @NotNull(message = "VALIDATION.REQUIRED")
    private LocalDate startDate;

    @NotNull(message = "VALIDATION.REQUIRED")
    private LocalDate endDate;
}


