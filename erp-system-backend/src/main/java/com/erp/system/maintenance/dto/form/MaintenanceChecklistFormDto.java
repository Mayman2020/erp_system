package com.erp.system.maintenance.dto.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MaintenanceChecklistFormDto {

    private Long id;

    @NotBlank
    private String itemText;

    private Boolean done;
    private Integer sortOrder;
}
