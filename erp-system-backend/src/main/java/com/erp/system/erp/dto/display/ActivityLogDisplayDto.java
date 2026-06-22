package com.erp.system.erp.dto.display;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ActivityLogDisplayDto {
    private Long id;
    private String moduleName;
    private String actionType;
    private String entityType;
    private Long entityId;
    private String entityReference;
    private String description;
    private String actor;
    private Instant createdAt;
}
