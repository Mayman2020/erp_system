package com.erp.system.accounting.dto.display;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationMatchPairDisplayDto {
    private Long id;
    private Long reconciliationId;
    private Long statementLineId;
    private Long systemLineId;
    private BigDecimal matchedAmount;
    private Instant matchedAt;
    private String matchedBy;
    private boolean active;
    private Instant unmatchedAt;
    private String unmatchedBy;
}
