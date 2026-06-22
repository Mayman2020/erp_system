package com.erp.system.projects.domain;

import com.erp.system.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "project_tasks", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectTask extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


@Column(name = "project_id", nullable = false)
private Long projectId;

@Column(name = "title", nullable = false, length = 300)
private String title;

@Column(name = "description", length = 1000)
private String description;

@Column(name = "assigned_employee_id")
private Long assignedEmployeeId;

@Column(name = "due_date")
private LocalDate dueDate;

@Enumerated(EnumType.STRING)
@Column(name = "status", nullable = false, length = 20)
@Builder.Default
private TaskStatus status = TaskStatus.TODO;

@Enumerated(EnumType.STRING)
@Column(name = "priority", nullable = false, length = 10)
@Builder.Default
private TaskPriority priority = TaskPriority.MEDIUM;

}
