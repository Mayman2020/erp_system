package com.erp.system.pmo.dto.display;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PmoRiskDisplayDto {
    Long id;
    Long projectId;
    String title;
    String severity;
    String status;
    String mitigation;
}
