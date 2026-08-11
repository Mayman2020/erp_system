package com.erp.system.hr.recruitment.dto.display;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
public class HrCandidateDisplayDto {
    Long id;
    String fullName;
    String email;
    String phone;
    Long vacancyId;
    String status;
    BigDecimal score;
    String notes;
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;
}
