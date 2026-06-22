package com.erp.system.inventory.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductFormDto {

    @NotBlank(message = "VALIDATION.REQUIRED")
    @Size(max = 50)
    private String code;

    @Size(max = 80)
    private String barcode;

    @NotBlank(message = "VALIDATION.REQUIRED")
    @Size(max = 200)
    private String nameEn;

    @Size(max = 200)
    private String nameAr;

    private Long categoryId;

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long unitId;

    @DecimalMin(value = "0", message = "VALIDATION.NON_NEGATIVE")
    private BigDecimal costPrice = BigDecimal.ZERO;

    @DecimalMin(value = "0", message = "VALIDATION.NON_NEGATIVE")
    private BigDecimal salePrice = BigDecimal.ZERO;

    @DecimalMin(value = "0", message = "VALIDATION.NON_NEGATIVE")
    private BigDecimal reorderLevel = BigDecimal.ZERO;

    private Boolean active = true;

    @Size(max = 500)
    private String description;
}
