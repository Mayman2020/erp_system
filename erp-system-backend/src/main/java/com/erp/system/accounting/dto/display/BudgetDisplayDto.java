package com.erp.system.accounting.dto.display;

import com.erp.system.common.enums.BudgetStatus;
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
public class BudgetDisplayDto {

    private Long id;
    private Long accountId;
    private String accountCode;
    private String accountName;
    private String budgetName;
    private Integer budgetYear;
    private Integer budgetMonth;
    private BigDecimal plannedAmount;
    private BigDecimal actualAmount;
    private BigDecimal variance;
    private BigDecimal variancePercentage;
    private boolean overBudget;
    private BudgetStatus status;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
}
