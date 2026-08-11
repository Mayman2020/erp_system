package com.erp.system.inventory.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class ProductUomConversionDisplayDto {
    private Long id;
    private Long productId;
    private Long unitId;
    private String unitCode;
    private String unitName;
    private BigDecimal factorToBase;
    private boolean purchase;
    private boolean sales;
    private Instant createdAt;
    private Instant updatedAt;
}
