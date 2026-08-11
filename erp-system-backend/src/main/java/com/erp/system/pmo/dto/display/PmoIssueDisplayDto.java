package com.erp.system.pmo.dto.display;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PmoIssueDisplayDto {
    Long id;
    Long projectId;
    String title;
    String status;
    String ownerName;
    String notes;
}
