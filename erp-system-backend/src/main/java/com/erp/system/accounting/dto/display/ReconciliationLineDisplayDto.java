package com.erp.system.accounting.dto.display;

import com.erp.system.common.enums.ReconciliationLineSourceType;
import com.erp.system.common.enums.ReconciliationLineStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationLineDisplayDto {

    private Long id;
    private LocalDate transactionDate;
    private String description;
    private BigDecimal amount;
    private ReconciliationLineSourceType transactionType;
    private ReconciliationLineStatus status;
    private String sourceReference;
    private Long journalEntryLineId;
    private Long matchedLineId;
    private BigDecimal matchedAmount;
}
