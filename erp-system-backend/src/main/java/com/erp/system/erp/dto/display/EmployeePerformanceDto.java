package com.erp.system.erp.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class EmployeePerformanceDto {
    private Long employeeId;
    private String employeeName;
    private BigDecimal salesAmount;
    private BigDecimal performancePercent;
}
