package com.erp.system.digitalliteracy.dto.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DigitalEnrollmentFormDto {
    @NotNull
    private Long courseId;
    @NotNull
    private Long employeeId;
    private BigDecimal progressPct;
    private BigDecimal score;
    private String status;
}
