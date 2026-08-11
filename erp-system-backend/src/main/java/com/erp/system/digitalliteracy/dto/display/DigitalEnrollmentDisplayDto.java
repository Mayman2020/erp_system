package com.erp.system.digitalliteracy.dto.display;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
public class DigitalEnrollmentDisplayDto {
    Long id;
    Long courseId;
    Long employeeId;
    BigDecimal progressPct;
    BigDecimal score;
    String status;
    Instant completedAt;
    String certificateNo;
}
