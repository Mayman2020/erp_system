package com.erp.system.organization.dto;

import com.erp.system.organization.domain.CompanyEntityType;

public record CompanyDto(
        Long id, String code, String nameEn, String nameAr,
        CompanyEntityType entityType, Long parentId, String parentName,
        String taxId, String registrationNo, String currencyCode,
        String countryCode, String email, String phone, String address,
        String logoUrl, boolean active, boolean defaultCompany) {
}
