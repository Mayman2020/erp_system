package com.erp.system.admin.dto.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LicenseActivateFormDto {
    @NotBlank
    private String licenseKey;
    @NotBlank
    private String customerName;
    private String modulesCsv;
    private Integer maxUsers;
    @NotBlank
    private String validFrom;
    @NotBlank
    private String validTo;
    private Integer graceDays;
    @NotBlank
    private String signature;
}
