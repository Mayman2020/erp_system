package com.erp.system.accounting.dto.display;

import com.erp.system.common.enums.InvoiceStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CustomerInvoiceDisplayDto {

    private Long id;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private String customerName;
    private String customerReference;
    private String description;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private InvoiceStatus status;
    private Long receivableAccountId;
    private String receivableAccountCode;
    private String receivableAccountName;
    private Long revenueAccountId;
    private String revenueAccountCode;
    private String revenueAccountName;
    private Long journalEntryId;
    private Long cancellationJournalEntryId;
    private LocalDateTime postedAt;
    private String postedBy;
    private LocalDateTime cancelledAt;
    private String cancelledBy;
    private List<CustomerInvoiceLineDisplayDto> lines;
}
