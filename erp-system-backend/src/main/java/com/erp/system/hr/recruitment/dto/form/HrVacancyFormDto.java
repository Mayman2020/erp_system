package com.erp.system.hr.recruitment.dto.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HrVacancyFormDto {
    @NotBlank
    private String title;
    private Long departmentId;
    @NotBlank
    private String status;
    @NotNull
    private Integer openings;
    private String description;
}
