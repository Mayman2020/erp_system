package com.erp.system.projects.dto.form;

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
public class ProjectMemberFormDto {

@NotNull
private Long projectId;

@NotNull
private Long employeeId;

@Size(max = 50)
private String role;

}
