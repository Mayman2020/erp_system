package com.erp.system.pmo.dto.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PmoMilestoneFormDto {
    @NotBlank
    private String title;
    private LocalDate dueDate;
    @NotBlank
    private String status;
    private Integer sortOrder;
}
