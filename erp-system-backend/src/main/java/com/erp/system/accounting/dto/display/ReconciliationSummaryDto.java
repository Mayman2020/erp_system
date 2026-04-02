package com.erp.system.accounting.dto.display;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationSummaryDto {
    private Long reconciliationId;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private BigDecimal systemEndingBalance;
    private BigDecimal difference;
    private long matchedCount;
    private long partiallyMatchedCount;
    private long unmatchedCount;
}
