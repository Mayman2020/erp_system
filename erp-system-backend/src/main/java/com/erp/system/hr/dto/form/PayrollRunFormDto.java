package com.erp.system.hr.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRunFormDto {
    @Size(max = 50)
    private String payrollNumber;

    @NotNull
    private LocalDate periodStart;

    @NotNull
    private LocalDate periodEnd;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal totalAmount;

    @Size(max = 500)
    private String notes;
}
