package com.erp.system.erp.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TopProductDto {
    private Long productId;
    private String productCode;
    private String productName;
    private BigDecimal quantitySold;
    private BigDecimal totalRevenue;
}
