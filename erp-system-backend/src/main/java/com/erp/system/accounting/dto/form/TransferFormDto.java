package com.erp.system.accounting.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransferFormDto {

    @NotNull(message = "VALIDATION.REQUIRED")
    private LocalDate transferDate;

    private String reference;

    private String description;

    @NotNull(message = "VALIDATION.REQUIRED")
    @DecimalMin(value = "0.01", message = "VALIDATION.AMOUNT_GT_ZERO")
    private BigDecimal amount;

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long sourceAccountId;

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long destinationAccountId;
}


