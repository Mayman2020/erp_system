package com.erp.system.inventory.dto.display;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDisplayDto {

    private Long id;
    private String code;
    private String barcode;
    private String name;
    private String nameEn;
    private String nameAr;
    private Long categoryId;
    private String categoryCode;
    private String categoryName;
    private Long unitId;
    private String unitCode;
    private String unitName;
    private BigDecimal costPrice;
    private BigDecimal salePrice;
    private BigDecimal reorderLevel;
    private boolean active;
    private String description;
    private BigDecimal totalQuantity;
    private Instant createdAt;
    private Instant updatedAt;
}
