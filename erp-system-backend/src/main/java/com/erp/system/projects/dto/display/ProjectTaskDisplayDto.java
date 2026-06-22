package com.erp.system.projects.dto.display;

import com.erp.system.projects.domain.TaskPriority;
import com.erp.system.projects.domain.TaskStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectTaskDisplayDto {
    private Long id;

private Long projectId;
private String title;
private String description;
private Long assignedEmployeeId;
private LocalDate dueDate;
private TaskStatus status;
private TaskPriority priority;

    private Instant createdAt;
    private Instant updatedAt;
}
