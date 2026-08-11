package com.erp.system.maintenance.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MaintenanceSparePartDisplayDto {

    private Long id;
    private Long ticketId;
    private Long productId;
    private String productCode;
    private String productName;
    private Long warehouseId;
    private String warehouseName;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private Long movementId;
    private boolean issued;
}
