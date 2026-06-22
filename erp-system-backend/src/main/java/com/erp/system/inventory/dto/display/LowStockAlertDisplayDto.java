package com.erp.system.inventory.dto.display;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockAlertDisplayDto {

    private Long productId;
    private String productCode;
    private String productName;
    private BigDecimal reorderLevel;
    private BigDecimal totalQuantity;
    private BigDecimal shortfall;
}
