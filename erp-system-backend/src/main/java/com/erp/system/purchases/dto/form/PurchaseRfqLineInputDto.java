package com.erp.system.purchases.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseRfqLineInputDto {

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long productId;

    @NotNull(message = "VALIDATION.REQUIRED")
    @DecimalMin(value = "0.0001", message = "VALIDATION.POSITIVE")
    private BigDecimal quantity;

    @Size(max = 300)
    private String notes;
}
