package com.erp.system.accounting.dto.display;

import com.erp.system.common.enums.TransferStatus;
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
public class TransferDisplayDto {

    private Long id;
    private LocalDate transferDate;
    private String reference;
    private String description;
    private BigDecimal amount;
    private TransferStatus status;
    private Long sourceAccountId;
    private String sourceAccountCode;
    private String sourceAccountName;
    private Long destinationAccountId;
    private String destinationAccountCode;
    private String destinationAccountName;
    private Long journalEntryId;
    private Long reversalJournalEntryId;
    private LocalDateTime postedAt;
    private String postedBy;
    private Instant createdAt;
    private Instant updatedAt;
}
