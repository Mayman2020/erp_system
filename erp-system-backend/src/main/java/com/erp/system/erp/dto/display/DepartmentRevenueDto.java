package com.erp.system.erp.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DepartmentRevenueDto {
    private String departmentName;
    private BigDecimal amount;
    private BigDecimal percent;
}
