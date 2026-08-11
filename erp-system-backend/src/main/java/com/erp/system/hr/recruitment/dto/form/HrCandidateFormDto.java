package com.erp.system.hr.recruitment.dto.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HrCandidateFormDto {
    @NotBlank
    private String fullName;
    private String email;
    private String phone;
    private Long vacancyId;
    @NotBlank
    private String status;
    private BigDecimal score;
    private String notes;
}
