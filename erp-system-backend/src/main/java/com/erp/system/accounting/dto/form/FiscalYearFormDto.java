package com.erp.system.accounting.dto.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FiscalYearFormDto {

    @NotNull(message = "VALIDATION.REQUIRED")
    private Integer year;

    @NotNull(message = "VALIDATION.REQUIRED")
    private LocalDate startDate;

    @NotNull(message = "VALIDATION.REQUIRED")
    private LocalDate endDate;
}


