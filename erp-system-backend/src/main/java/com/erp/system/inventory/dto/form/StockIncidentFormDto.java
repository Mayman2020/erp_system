package com.erp.system.inventory.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockIncidentFormDto {

    private String incidentNo;

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long warehouseId;

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long productId;

    @NotNull(message = "VALIDATION.REQUIRED")
    @DecimalMin(value = "0.0001", message = "VALIDATION.POSITIVE")
    private BigDecimal quantity;

    @NotBlank(message = "VALIDATION.REQUIRED")
    @Size(max = 20)
    private String incidentType;

    @Size(max = 40)
    private String reasonCode;

    @Size(max = 500)
    private String notes;

    @DecimalMin(value = "0", message = "VALIDATION.NON_NEGATIVE")
    private BigDecimal unitCost;
}
