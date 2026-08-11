package com.erp.system.inventory.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUomConversionFormDto {

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long unitId;

    @NotNull(message = "VALIDATION.REQUIRED")
    @DecimalMin(value = "0.000001", message = "VALIDATION.POSITIVE")
    private BigDecimal factorToBase;

    private Boolean purchase;
    private Boolean sales;
}
