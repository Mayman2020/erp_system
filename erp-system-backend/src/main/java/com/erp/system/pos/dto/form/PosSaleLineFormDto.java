package com.erp.system.pos.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PosSaleLineFormDto {
    @NotNull
    private Long productId;
    @NotNull
    @DecimalMin("0.0001")
    private BigDecimal quantity;
    @NotNull
    @DecimalMin("0")
    private BigDecimal unitPrice;
    @DecimalMin("0")
    private BigDecimal discountAmount = BigDecimal.ZERO;
    @DecimalMin("0")
    private BigDecimal taxRate = BigDecimal.ZERO;
}
