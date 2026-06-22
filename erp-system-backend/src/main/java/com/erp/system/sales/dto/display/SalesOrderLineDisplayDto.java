package com.erp.system.sales.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SalesOrderLineDisplayDto {

    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountPercent;
    private BigDecimal taxPercent;
    private BigDecimal lineTotal;
}
