package com.erp.system.manufacturing.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class WorkOrderDisplayDto {
    private Long id;
    private String orderNumber;
    private Long productId;
    private String productCode;
    private String productName;
    private Long warehouseId;
    private String warehouseName;
    private BigDecimal quantity;
    private BigDecimal producedQuantity;
    private String status;
    private LocalDate plannedStart;
    private LocalDate plannedEnd;
    private String notes;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
