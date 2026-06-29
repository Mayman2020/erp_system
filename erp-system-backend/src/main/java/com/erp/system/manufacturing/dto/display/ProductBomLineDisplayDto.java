package com.erp.system.manufacturing.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class ProductBomLineDisplayDto {
    private Long id;
    private Long parentProductId;
    private String parentProductCode;
    private String parentProductName;
    private Long componentProductId;
    private String componentProductCode;
    private String componentProductName;
    private BigDecimal quantityPerUnit;
    private Instant createdAt;
    private Instant updatedAt;
}
