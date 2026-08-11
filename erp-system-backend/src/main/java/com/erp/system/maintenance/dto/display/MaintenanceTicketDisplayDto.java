package com.erp.system.maintenance.dto.display;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MaintenanceTicketDisplayDto {

    private Long id;
    private String ticketNo;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private Long customerId;
    private String customerName;
    private String title;
    private String description;
    private String priority;
    private String status;
    private String ticketType;
    private Long technicianId;
    private String technicianName;
    private Integer slaHours;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private List<MaintenanceChecklistDisplayDto> checklists;
    private List<MaintenanceSparePartDisplayDto> spareParts;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
