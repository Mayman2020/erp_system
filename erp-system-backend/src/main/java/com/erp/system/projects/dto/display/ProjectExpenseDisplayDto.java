package com.erp.system.projects.dto.display;

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
public class ProjectExpenseDisplayDto {
    private Long id;

private Long projectId;
private LocalDate expenseDate;
private String description;
private BigDecimal amount;
private TransactionStatus status;
private Long journalEntryId;

    private Instant createdAt;
    private Instant updatedAt;
}
