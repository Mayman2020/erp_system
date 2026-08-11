package com.erp.system.purchases.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class GoodsReceiptDisplayDto {
    private Long id;
    private String receiptNo;
    private Long supplierId;
    private String supplierName;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private Long purchaseOrderId;
    private String status;
    private Instant receivedAt;
    private String notes;
    private List<GoodsReceiptLineDisplayDto> lines;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
