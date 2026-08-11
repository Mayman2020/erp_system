package com.erp.system.maintenance.dto.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MaintenanceTechnicianFormDto {

    private Long employeeId;

    @NotBlank
    private String displayName;

    private String skillsCsv;
    private Boolean active;
}
