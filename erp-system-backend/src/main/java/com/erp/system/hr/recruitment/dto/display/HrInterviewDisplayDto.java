package com.erp.system.hr.recruitment.dto.display;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDateTime;

@Value
@Builder
public class HrInterviewDisplayDto {
    Long id;
    Long candidateId;
    LocalDateTime scheduledAt;
    String interviewer;
    String result;
    String notes;
    Instant createdAt;
}
