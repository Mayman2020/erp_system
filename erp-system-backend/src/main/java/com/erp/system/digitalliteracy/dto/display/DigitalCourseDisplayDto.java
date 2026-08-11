package com.erp.system.digitalliteracy.dto.display;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class DigitalCourseDisplayDto {
    Long id;
    String code;
    String title;
    String description;
    boolean active;
    Instant createdAt;
}
