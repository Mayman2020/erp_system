package com.erp.system.partners.dto.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProfitDistributionFormDto {

    private String distributionNo;

    @NotBlank
    private String periodLabel;

    private BigDecimal totalProfit;

    /** When set, profit is pulled from accounting P&L for this range instead of totalProfit. */
    private LocalDate profitFromDate;
    private LocalDate profitToDate;
}
