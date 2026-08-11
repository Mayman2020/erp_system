package com.erp.system.maintenance.dto.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MaintenanceAssetFormDto {

    @NotBlank
    private String assetCode;

    @NotBlank
    private String name;

    private String serialNo;
    private Long customerId;
    private String status;
    private String notes;
}
