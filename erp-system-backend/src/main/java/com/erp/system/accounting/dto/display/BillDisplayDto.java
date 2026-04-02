package com.erp.system.accounting.dto.display;

import com.erp.system.common.enums.BillStatus;
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
public class BillDisplayDto {

    private Long id;
    private String billNumber;
    private LocalDate billDate;
    private LocalDate dueDate;
    private String supplierName;
    private String supplierReference;
    private String description;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private BillStatus status;
    private Long payableAccountId;
    private String payableAccountCode;
    private String payableAccountName;
    private Long taxAccountId;
    private String taxAccountCode;
    private String taxAccountName;
    private Long journalEntryId;
    private Long cancellationJournalEntryId;
    private LocalDateTime approvedAt;
    private String approvedBy;
    private LocalDateTime postedAt;
    private String postedBy;
    private List<BillLineDisplayDto> lines;
    private Instant createdAt;
    private Instant updatedAt;
}
