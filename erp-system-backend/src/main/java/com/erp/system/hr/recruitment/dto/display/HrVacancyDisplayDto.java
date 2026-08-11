package com.erp.system.hr.recruitment.dto.display;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class HrVacancyDisplayDto {
    Long id;
    String title;
    Long departmentId;
    String status;
    Integer openings;
    String description;
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;
}
