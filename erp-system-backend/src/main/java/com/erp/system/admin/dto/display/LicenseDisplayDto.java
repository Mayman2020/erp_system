package com.erp.system.admin.dto.display;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDate;

@Value
@Builder
public class LicenseDisplayDto {
    Long id;
    String licenseKey;
    String customerName;
    String modulesCsv;
    Integer maxUsers;
    LocalDate validFrom;
    LocalDate validTo;
    Integer graceDays;
    boolean active;
    boolean valid;
    Instant activatedAt;
}
