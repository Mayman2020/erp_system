package com.erp.system.hr.recruitment.dto.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HrInterviewFormDto {
    @NotNull
    private Long candidateId;
    @NotNull
    private LocalDateTime scheduledAt;
    private String interviewer;
    private String result;
    private String notes;
}
