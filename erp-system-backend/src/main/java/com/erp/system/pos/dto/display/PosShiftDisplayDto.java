package com.erp.system.pos.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PosShiftDisplayDto {
    private Long id;
    private String shiftNo;
    private Long terminalId;
    private String terminalCode;
    private Long warehouseId;
    private String warehouseName;
    private Long cashierUserId;
    private String cashierName;
    private String status;
    private BigDecimal openingCash;
    private BigDecimal closingCash;
    private BigDecimal expectedCash;
    private BigDecimal cashSales;
    private BigDecimal cardSales;
    private BigDecimal creditSales;
    private BigDecimal discrepancy;
    private String notes;
    private Instant openedAt;
    private Instant closedAt;
}
