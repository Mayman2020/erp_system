package com.erp.system.accounting.dto.form;

import com.erp.system.common.enums.BudgetStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetFormDto {

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long accountId;

    private String budgetName;

    @NotNull(message = "VALIDATION.REQUIRED")
    private Integer budgetYear;

    @Min(value = 1, message = "VALIDATION.MONTH_RANGE")
    @Max(value = 12, message = "VALIDATION.MONTH_RANGE")
    private Integer budgetMonth;

    @NotNull(message = "VALIDATION.REQUIRED")
    @DecimalMin(value = "0.00", message = "VALIDATION.NON_NEGATIVE")
    private BigDecimal plannedAmount;

    @NotNull(message = "VALIDATION.REQUIRED")
    private BudgetStatus status;

    private String notes;
}


