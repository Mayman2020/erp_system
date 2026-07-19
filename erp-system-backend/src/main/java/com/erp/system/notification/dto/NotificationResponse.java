package com.erp.system.notification.dto;

import com.erp.system.notification.domain.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String titleKey;
    private String bodyKey;
    private String varsJson;
    private String referenceType;
    private Long referenceId;
    private boolean read;
    private Instant readAt;
    private Instant createdAt;
}
