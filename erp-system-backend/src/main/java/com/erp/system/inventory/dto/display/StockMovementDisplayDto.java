package com.erp.system.inventory.dto.display;

import com.erp.system.common.enums.StockMovementType;
import com.erp.system.common.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementDisplayDto {

    private Long id;
    private String movementNumber;
    private LocalDate movementDate;
    private StockMovementType movementType;
    private Long productId;
    private String productCode;
    private String productName;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private Long targetWarehouseId;
    private String targetWarehouseCode;
    private String targetWarehouseName;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private String referenceType;
    private Long referenceId;
    private String notes;
    private TransactionStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
