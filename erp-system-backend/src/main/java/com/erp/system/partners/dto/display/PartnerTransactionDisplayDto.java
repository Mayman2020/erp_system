package com.erp.system.partners.dto.display;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerTransactionDisplayDto {
    private Long id;
    private Long partnerId;
    private String partnerCode;
    private String partnerName;
    private String txnType;
    private BigDecimal amount;
    private LocalDate txnDate;
    private String notes;
    private String status;
    private Long journalEntryId;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
