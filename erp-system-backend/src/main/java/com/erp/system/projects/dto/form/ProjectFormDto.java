package com.erp.system.projects.dto.form;

import com.erp.system.projects.domain.ProjectStatus;
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
public class ProjectFormDto {

@NotBlank
@Size(max = 50)
private String projectCode;

@NotBlank
@Size(max = 200)
private String nameEn;

@Size(max = 200)
private String nameAr;

private Long customerId;
private LocalDate startDate;
private LocalDate endDate;

@NotNull
@DecimalMin("0.0")
private BigDecimal budget;

@NotNull
private ProjectStatus status;

@Size(max = 1000)
private String description;

}
