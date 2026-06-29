package com.erp.system.hr.dto.display;

import com.erp.system.common.enums.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRunDisplayDto {
    private Long id;
    private String payrollNumber;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private TransactionStatus status;
    private BigDecimal totalAmount;
    private String notes;
    private Long journalEntryId;
    private List<PayrollLineDisplayDto> lines;
    private Instant createdAt;
    private Instant updatedAt;
}
