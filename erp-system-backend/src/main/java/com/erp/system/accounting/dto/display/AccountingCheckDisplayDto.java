package com.erp.system.accounting.dto.display;

import com.erp.system.common.enums.CheckStatus;
import com.erp.system.common.enums.CheckType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountingCheckDisplayDto {

    private Long id;
    private String checkNumber;
    private CheckType checkType;
    private String bankName;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private BigDecimal amount;
    private CheckStatus status;
    private String partyName;
    private String linkedDocumentReference;
    private Long bankAccountId;
    private String bankAccountNumber;
    private Long holdingAccountId;
    private String holdingAccountCode;
    private String holdingAccountName;
    private Long journalEntryId;
    private Long reversalJournalEntryId;
    private LocalDateTime clearedAt;
    private LocalDateTime bouncedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
