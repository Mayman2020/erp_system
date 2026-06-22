package com.erp.system.projects.dto.display;

import com.erp.system.projects.domain.ProjectStatus;
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
public class ProjectDisplayDto {
    private Long id;

private String projectCode;
private String nameEn;
private String nameAr;
private Long customerId;
private LocalDate startDate;
private LocalDate endDate;
private BigDecimal budget;
private ProjectStatus status;
private String description;

    private Instant createdAt;
    private Instant updatedAt;
}
