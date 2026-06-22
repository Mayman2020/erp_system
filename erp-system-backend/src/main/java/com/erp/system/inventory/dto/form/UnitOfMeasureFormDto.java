package com.erp.system.inventory.dto.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UnitOfMeasureFormDto {

    @NotBlank(message = "VALIDATION.REQUIRED")
    @Size(max = 20)
    private String code;

    @NotBlank(message = "VALIDATION.REQUIRED")
    @Size(max = 80)
    private String nameEn;

    @Size(max = 80)
    private String nameAr;

    private Boolean active = true;
}
