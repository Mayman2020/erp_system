package com.erp.system.partners.dto.display;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitDistributionDisplayDto {
    private Long id;
    private String distributionNo;
    private String periodLabel;
    private BigDecimal totalProfit;
    private String status;
    private LocalDateTime approvedAt;
    private Long journalEntryId;
    private List<ProfitDistributionLineDisplayDto> lines;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
