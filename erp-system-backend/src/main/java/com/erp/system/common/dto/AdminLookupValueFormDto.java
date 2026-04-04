package com.erp.system.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminLookupValueFormDto {

    @NotBlank
    @Size(max = 60)
    private String typeCode;

    @NotBlank
    @Size(max = 80)
    private String code;

    @NotBlank
    @Size(max = 150)
    private String nameEn;

    @NotBlank
    @Size(max = 150)
    private String nameAr;

    private Integer sortOrder = 0;

    private Boolean active = true;
}
