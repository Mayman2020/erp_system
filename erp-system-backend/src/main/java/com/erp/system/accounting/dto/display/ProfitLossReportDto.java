package com.erp.system.accounting.dto.display;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitLossReportDto {
    private LocalDate fromDate;
    private LocalDate toDate;
    private String reportCurrency;
    private List<ProfitLossLineDto> revenues;
    private List<ProfitLossLineDto> expenses;
    private BigDecimal totalRevenue;
    private BigDecimal totalExpenses;
    private BigDecimal netProfit;
}
