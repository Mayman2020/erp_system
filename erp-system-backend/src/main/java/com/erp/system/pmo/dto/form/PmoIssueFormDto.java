package com.erp.system.pmo.dto.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PmoIssueFormDto {
    @NotBlank
    private String title;
    @NotBlank
    private String status;
    private String ownerName;
    private String notes;
}
