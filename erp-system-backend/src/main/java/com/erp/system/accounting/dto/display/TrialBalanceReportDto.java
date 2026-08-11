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
public class TrialBalanceReportDto {
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<TrialBalanceLineDto> lines;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private boolean balanced;
}
