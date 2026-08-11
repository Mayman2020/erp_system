package com.erp.system.maintenance.dto.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class MaintenanceTicketFormDto {

    private String ticketNo;
    private Long assetId;
    private Long customerId;

    @NotBlank
    private String title;

    private String description;
    private String priority;
    private String ticketType;
    private Long technicianId;
    private Integer slaHours;
    private List<MaintenanceChecklistFormDto> checklists;
}
