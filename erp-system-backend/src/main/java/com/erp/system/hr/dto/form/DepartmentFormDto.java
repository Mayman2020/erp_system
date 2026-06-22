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
public class DepartmentFormDto {

@NotBlank
@Size(max = 30)
private String code;

@NotBlank
@Size(max = 150)
private String nameEn;

@Size(max = 150)
private String nameAr;

private Long managerId;
private Boolean active;

}
