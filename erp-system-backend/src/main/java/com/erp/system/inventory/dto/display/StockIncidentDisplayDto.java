package com.erp.system.inventory.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class StockIncidentDisplayDto {
    private Long id;
    private String incidentNo;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private Long productId;
    private String productCode;
    private String productName;
    private BigDecimal quantity;
    private String incidentType;
    private String reasonCode;
    private String notes;
    private BigDecimal unitCost;
    private BigDecimal financialImpact;
    private String status;
    private String approvedBy;
    private Instant approvedAt;
    private Long movementId;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
