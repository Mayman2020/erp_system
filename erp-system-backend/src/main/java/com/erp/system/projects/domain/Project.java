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
@Table(name = "projects", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


@Column(name = "project_code", nullable = false, length = 50, unique = true)
private String projectCode;

@Column(name = "name_en", nullable = false, length = 200)
private String nameEn;

@Column(name = "name_ar", length = 200)
private String nameAr;

@Column(name = "customer_id")
private Long customerId;

@Column(name = "start_date")
private LocalDate startDate;

@Column(name = "end_date")
private LocalDate endDate;

@Column(name = "budget", nullable = false, precision = 19, scale = 2)
@Builder.Default
private BigDecimal budget = BigDecimal.ZERO;

@Enumerated(EnumType.STRING)
@Column(name = "status", nullable = false, length = 20)
@Builder.Default
private ProjectStatus status = ProjectStatus.PLANNING;

@Column(name = "description", length = 1000)
private String description;

}
