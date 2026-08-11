package com.erp.system.maintenance.dto.display;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MaintenanceChecklistDisplayDto {

    private Long id;
    private Long ticketId;
    private String itemText;
    private boolean done;
    private int sortOrder;
}
