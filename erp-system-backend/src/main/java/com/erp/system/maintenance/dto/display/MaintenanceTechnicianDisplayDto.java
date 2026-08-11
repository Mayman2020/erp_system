package com.erp.system.maintenance.dto.display;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class MaintenanceTechnicianDisplayDto {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String displayName;
    private String skillsCsv;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
