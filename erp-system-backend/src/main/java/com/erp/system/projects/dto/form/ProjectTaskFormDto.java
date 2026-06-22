package com.erp.system.projects.dto.form;

import com.erp.system.projects.domain.TaskPriority;
import com.erp.system.projects.domain.TaskStatus;
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
public class ProjectTaskFormDto {

@NotNull
private Long projectId;

@NotBlank
@Size(max = 300)
private String title;

@Size(max = 1000)
private String description;

private Long assignedEmployeeId;
private LocalDate dueDate;

@NotNull
private TaskStatus status;

@NotNull
private TaskPriority priority;

}
