package com.erp.system.hr.recruitment.dto.display;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class LeaveBalanceDisplayDto {
    Long id;
    Long employeeId;
    String leaveType;
    BigDecimal balanceDays;
    Integer year;
}
