package com.erp.system.accounting.dto.display;

import com.erp.system.common.enums.JournalEntryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryDisplayDto {

    private Long id;
    private String referenceNumber;
    private LocalDate entryDate;
    private String description;
    private String externalReference;
    private String currencyCode;
    private String entryType;
    private JournalEntryStatus status;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private Boolean balanced;
    private LocalDateTime postedAt;
    private String postedBy;
    private LocalDateTime reversedAt;
    private String reversedBy;
    private String reversalReference;
    private String sourceModule;
    private Long sourceRecordId;
    private List<JournalEntryLineDisplayDto> lines;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
