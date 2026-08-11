package com.erp.system.alerts.dto.display;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class AlertEventDisplayDto {
    Long id;
    Long ruleId;
    String title;
    String body;
    String severity;
    String entityType;
    String entityRef;
    String deepLink;
    String status;
    Instant createdAt;
    Instant acknowledgedAt;
}
