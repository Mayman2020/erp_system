package com.erp.system.hr.dto.display;

import com.erp.system.common.enums.TransactionStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestDisplayDto {
    private Long id;
    private Long employeeId;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private TransactionStatus status;
    private String reason;
    private Instant createdAt;
    private Instant updatedAt;
}
