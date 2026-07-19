package com.erp.system.common.dto;

import lombok.Builder;

@Builder
public record CompanySettingsDto(
        String companyNameEn,
        String companyNameAr,
        String taxId,
        String logoBase64,
        Integer fiscalYearStartMonth
) {
}
