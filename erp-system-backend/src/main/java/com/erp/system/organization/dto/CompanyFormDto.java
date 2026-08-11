package com.erp.system.organization.dto;

import com.erp.system.organization.domain.CompanyEntityType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CompanyFormDto {
    @NotBlank @Size(max = 40)
    private String code;
    @NotBlank @Size(max = 190)
    private String nameEn;
    @NotBlank @Size(max = 190)
    private String nameAr;
    @NotNull
    private CompanyEntityType entityType;
    private Long parentId;
    @Size(max = 80) private String taxId;
    @Size(max = 80) private String registrationNo;
    @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$") private String currencyCode = "AED";
    @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$") private String countryCode = "AE";
    @Email @Size(max = 190) private String email;
    @Size(max = 40) private String phone;
    @Size(max = 2000) private String address;
    @Size(max = 500) private String logoUrl;
    private Boolean active = true;
}
