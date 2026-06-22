package com.erp.system.inventory.dto.form;

import com.erp.system.common.enums.StockMovementType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StockMovementFormDto {

    private String movementNumber;

    @NotNull(message = "VALIDATION.REQUIRED")
    private LocalDate movementDate;

    @NotNull(message = "VALIDATION.REQUIRED")
    private StockMovementType movementType;

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long productId;

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long warehouseId;

    private Long targetWarehouseId;

    @NotNull(message = "VALIDATION.REQUIRED")
    private BigDecimal quantity;

    @DecimalMin(value = "0", message = "VALIDATION.NON_NEGATIVE")
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Size(max = 50)
    private String referenceType;

    private Long referenceId;

    @Size(max = 500)
    private String notes;

    private Boolean approveImmediately = false;
}
