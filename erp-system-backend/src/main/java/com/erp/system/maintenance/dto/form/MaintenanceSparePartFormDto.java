package com.erp.system.maintenance.dto.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MaintenanceSparePartFormDto {

    @NotNull
    private Long productId;

    @NotNull
    private Long warehouseId;

    @NotNull
    private BigDecimal quantity;

    private BigDecimal unitCost;
}
