package com.erp.system.hr.dto.display;

import com.erp.system.common.enums.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollLineDisplayDto {
    private Long id;

private Long payrollId;
private Long employeeId;
private BigDecimal basicSalary;
private BigDecimal allowances;
private BigDecimal deductions;
private BigDecimal netSalary;

    private Instant createdAt;
    private Instant updatedAt;
}
