package com.erp.system.manufacturing.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductBomLineFormDto {

    @NotNull
    private Long parentProductId;

    @NotNull
    private Long componentProductId;

    @NotNull
    @DecimalMin("0.0001")
    private BigDecimal quantityPerUnit;
}
