package com.erp.system.accounting.dto.display;

import com.erp.system.common.enums.ReconciliationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationDisplayDto {

    private Long id;
    private Long bankAccountId;
    private String bankAccountNumber;
    private LocalDate statementStartDate;
    private LocalDate statementEndDate;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private BigDecimal systemEndingBalance;
    private BigDecimal difference;
    private ReconciliationStatus status;
    private long matchedCount;
    private long partiallyMatchedCount;
    private long unmatchedCount;
    private LocalDateTime finalizedAt;
    private String finalizedBy;
    private List<ReconciliationLineDisplayDto> lines;
    private Instant createdAt;
    private Instant updatedAt;
}
