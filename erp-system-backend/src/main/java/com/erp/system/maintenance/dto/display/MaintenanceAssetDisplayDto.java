package com.erp.system.maintenance.dto.display;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class MaintenanceAssetDisplayDto {

    private Long id;
    private String assetCode;
    private String name;
    private String serialNo;
    private Long customerId;
    private String customerName;
    private String status;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
