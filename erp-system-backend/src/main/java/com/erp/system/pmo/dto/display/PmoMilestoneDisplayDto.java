package com.erp.system.pmo.dto.display;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class PmoMilestoneDisplayDto {
    Long id;
    Long projectId;
    String title;
    LocalDate dueDate;
    String status;
    Integer sortOrder;
}
