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
public class ProjectExpenseFormDto {

@NotNull
private Long projectId;

@NotNull
private LocalDate expenseDate;

@NotBlank
@Size(max = 500)
private String description;

@NotNull
@DecimalMin("0.01")
private BigDecimal amount;

}
