package com.erp.system.sales.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesQuotationLineFormDto {

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long productId;

    @Size(max = 500)
    private String description;

    @NotNull(message = "VALIDATION.REQUIRED")
    @DecimalMin(value = "0.0001", message = "VALIDATION.POSITIVE")
    private BigDecimal quantity;

    @NotNull(message = "VALIDATION.REQUIRED")
    @DecimalMin(value = "0.00", message = "VALIDATION.NON_NEGATIVE")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.00", message = "VALIDATION.NON_NEGATIVE")
    private BigDecimal discountPercent;

    @DecimalMin(value = "0.00", message = "VALIDATION.NON_NEGATIVE")
    private BigDecimal taxPercent;
}
