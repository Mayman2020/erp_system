package com.erp.system.purchases.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PurchaseRfqLineDisplayDto {
    private Long id;
    private Long rfqId;
    private Long productId;
    private String productCode;
    private String productName;
    private BigDecimal quantity;
    private String notes;
}
