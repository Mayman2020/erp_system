package com.erp.system.accounting.dto.display;

import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.common.enums.TransactionType;
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
public class AccountingTransactionDisplayDto {

    private Long id;
    private LocalDate transactionDate;
    private String reference;
    private String description;
    private TransactionType transactionType;
    private TransactionStatus status;
    private BigDecimal amount;
    private Long debitAccountId;
    private String debitAccountCode;
    private String debitAccountName;
    private String debitAccountNameEn;
    private String debitAccountNameAr;
    private Long creditAccountId;
    private String creditAccountCode;
    private String creditAccountName;
    private String creditAccountNameEn;
    private String creditAccountNameAr;
    private Long originalTransactionId;
    private String relatedDocumentReference;
    private Long journalEntryId;
    private LocalDateTime postedAt;
    private String postedBy;
    private Instant createdAt;
    private Instant updatedAt;
}
