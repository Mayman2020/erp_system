package com.erp.system.maintenance.dto.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignTechnicianFormDto {

    @NotNull
    private Long technicianId;
}
