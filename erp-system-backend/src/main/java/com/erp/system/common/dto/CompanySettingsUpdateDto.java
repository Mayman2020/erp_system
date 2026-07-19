package com.erp.system.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanySettingsUpdateDto {

    @NotBlank
    @Size(max = 200)
    private String companyNameEn;

    @NotBlank
    @Size(max = 200)
    private String companyNameAr;

    @Size(max = 60)
    private String taxId;

    private String logoBase64;

    @Min(1)
    @Max(12)
    private Integer fiscalYearStartMonth;
}
