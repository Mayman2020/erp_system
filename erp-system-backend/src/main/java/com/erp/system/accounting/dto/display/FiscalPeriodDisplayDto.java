package com.erp.system.accounting.dto.display;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiscalPeriodDisplayDto {

    private Long id;
    private Long fiscalYearId;
    private String periodName;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean open;
    private LocalDateTime closedAt;
    private String closedBy;
    private Instant createdAt;
    private Instant updatedAt;
}
