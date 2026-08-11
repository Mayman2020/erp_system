package com.erp.system.purchases.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class GoodsReceiptLineDisplayDto {
    private Long id;
    private Long receiptId;
    private Long productId;
    private String productCode;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal unitCost;
}
