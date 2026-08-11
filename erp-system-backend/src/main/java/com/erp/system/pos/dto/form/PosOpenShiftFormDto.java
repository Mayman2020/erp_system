package com.erp.system.pos.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PosOpenShiftFormDto {
    @NotNull
    private Long terminalId;
    private Long warehouseId;
    @NotNull
    @DecimalMin("0")
    private BigDecimal openingCash = BigDecimal.ZERO;
    private String notes;
}
