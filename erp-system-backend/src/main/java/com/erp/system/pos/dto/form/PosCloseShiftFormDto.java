package com.erp.system.pos.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PosCloseShiftFormDto {
    @NotNull
    @DecimalMin("0")
    private BigDecimal closingCash;
    private String notes;
}
