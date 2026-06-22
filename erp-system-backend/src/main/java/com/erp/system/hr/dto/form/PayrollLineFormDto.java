package com.erp.system.hr.dto.form;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollLineFormDto {

@NotNull
private Long payrollId;

@NotNull
private Long employeeId;

@NotNull
@DecimalMin("0.0")
private BigDecimal basicSalary;

@DecimalMin("0.0")
private BigDecimal allowances;

@DecimalMin("0.0")
private BigDecimal deductions;

@NotNull
@DecimalMin("0.0")
private BigDecimal netSalary;

}
