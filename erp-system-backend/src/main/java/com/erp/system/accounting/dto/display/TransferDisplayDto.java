package com.erp.system.accounting.dto.display;

import com.erp.system.common.enums.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferDisplayDto {
    private Long id;
    private LocalDate transferDate;
    private String reference;
    private String description;
    private BigDecimal amount;
    private Long sourceAccountId;
    private String sourceAccountCode;
    private String sourceAccountName;
    private Long destinationAccountId;
    private String destinationAccountCode;
    private String destinationAccountName;
    private TransactionStatus status;
    private LocalDateTime postedAt;
    private String postedBy;
    private Long journalEntryId;
    private Instant createdAt;
    private Instant updatedAt;
}
