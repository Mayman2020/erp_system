package com.erp.system.accounting.dto.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ReconciliationFormDto {

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long bankAccountId;

    @NotNull(message = "VALIDATION.REQUIRED")
    private LocalDate statementStartDate;

    @NotNull(message = "VALIDATION.REQUIRED")
    private LocalDate statementEndDate;

    @NotNull(message = "VALIDATION.REQUIRED")
    private BigDecimal openingBalance;

    @NotNull(message = "VALIDATION.REQUIRED")
    private BigDecimal closingBalance;

    @Valid
    private List<ReconciliationLineFormDto> statementLines;
}


