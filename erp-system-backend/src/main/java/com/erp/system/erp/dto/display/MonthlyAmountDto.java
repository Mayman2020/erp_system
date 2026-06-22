package com.erp.system.erp.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MonthlyAmountDto {
    private String month;
    private BigDecimal amount;
}
