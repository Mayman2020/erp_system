package com.erp.system.manufacturing.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class WorkOrderFormDto {

    @Size(max = 50)
    private String orderNumber;

    @NotNull
    private Long productId;

    private Long warehouseId;

    @NotNull
    @DecimalMin("0.0001")
    private BigDecimal quantity;

    private LocalDate plannedStart;
    private LocalDate plannedEnd;

    @Size(max = 500)
    private String notes;
}
