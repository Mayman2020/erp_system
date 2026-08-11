package com.erp.system.purchases.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PurchaseRfqQuoteDisplayDto {
    private Long id;
    private Long rfqId;
    private Long supplierId;
    private String supplierName;
    private BigDecimal unitPrice;
    private int leadDays;
    private String notes;
    private boolean selected;
    private Instant createdAt;
}
