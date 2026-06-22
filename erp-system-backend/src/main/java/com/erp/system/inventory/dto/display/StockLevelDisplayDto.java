package com.erp.system.inventory.dto.display;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLevelDisplayDto {

    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private BigDecimal quantity;
    private BigDecimal reservedQuantity;
    private BigDecimal availableQuantity;
    private Instant createdAt;
    private Instant updatedAt;
}
