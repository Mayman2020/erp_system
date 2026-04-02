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
public class BalanceSheetReportDto {
    private LocalDate asOfDate;
    private List<BalanceSheetLineDto> assets;
    private List<BalanceSheetLineDto> liabilities;
    private List<BalanceSheetLineDto> equity;
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal totalEquity;
    private BigDecimal liabilitiesAndEquity;
    private boolean balanced;
}
