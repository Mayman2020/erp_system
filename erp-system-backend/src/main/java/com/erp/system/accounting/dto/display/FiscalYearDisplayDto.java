package com.erp.system.accounting.dto.display;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiscalYearDisplayDto {

    private Long id;
    private Integer year;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean open;
    private LocalDateTime closedAt;
    private String closedBy;
    private List<FiscalPeriodDisplayDto> periods;
    private Instant createdAt;
    private Instant updatedAt;
}
