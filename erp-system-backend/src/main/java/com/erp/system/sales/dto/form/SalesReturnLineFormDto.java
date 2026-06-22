package com.erp.system.sales.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesReturnLineFormDto {

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long productId;

    @NotNull(message = "VALIDATION.REQUIRED")
    @DecimalMin(value = "0.0001", message = "VALIDATION.POSITIVE")
    private BigDecimal quantity;

    @NotNull(message = "VALIDATION.REQUIRED")
    @DecimalMin(value = "0.00", message = "VALIDATION.NON_NEGATIVE")
    private BigDecimal unitPrice;
}
