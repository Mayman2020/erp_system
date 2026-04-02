package com.erp.system.accounting.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillLineFormDto {

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long accountId;

    private String description;

    @NotNull(message = "VALIDATION.REQUIRED")
    @DecimalMin(value = "0.01", message = "VALIDATION.AMOUNT_GT_ZERO")
    private BigDecimal quantity;

    @NotNull(message = "VALIDATION.REQUIRED")
    @DecimalMin(value = "0.00", message = "VALIDATION.NON_NEGATIVE")
    private BigDecimal unitPrice;
}


