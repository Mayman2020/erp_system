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
public class EmployeeFormDto {

@NotBlank
@Size(max = 50)
private String employeeCode;

@NotBlank
@Size(max = 200)
private String fullNameEn;

@Size(max = 200)
private String fullNameAr;

@Email
@Size(max = 190)
private String email;

@Size(max = 30)
private String phone;

private Long departmentId;

@Size(max = 150)
private String jobTitle;

private LocalDate hireDate;

@NotNull
@DecimalMin("0.0")
private BigDecimal basicSalary;

private Boolean active;

}
