package com.erp.system.digitalliteracy.dto.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DigitalCourseFormDto {
    @NotBlank
    private String code;
    @NotBlank
    private String title;
    private String description;
    private Boolean active;
}
