package com.erp.system.inventory.dto.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductCategoryFormDto {

    @NotBlank(message = "VALIDATION.REQUIRED")
    @Size(max = 30)
    private String code;

    @NotBlank(message = "VALIDATION.REQUIRED")
    @Size(max = 150)
    private String nameEn;

    @Size(max = 150)
    private String nameAr;

    private Long parentId;

    private Boolean active = true;
}
