package com.erp.system.inventory.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class ReplenishmentProposalDisplayDto {
    private Long id;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private Long productId;
    private String productCode;
    private String productName;
    private BigDecimal currentQty;
    private BigDecimal reorderLevel;
    private BigDecimal proposedQty;
    private String status;
    private Long purchaseOrderId;
    private Instant createdAt;
    private Instant updatedAt;
}
