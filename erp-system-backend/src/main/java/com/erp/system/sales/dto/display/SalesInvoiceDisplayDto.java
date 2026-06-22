package com.erp.system.sales.dto.display;

import com.erp.system.common.enums.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SalesInvoiceDisplayDto {

    private Long id;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private Long customerId;
    private String customerCode;
    private String customerName;
    private Long orderId;
    private String orderNumber;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private TransactionStatus status;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private String notes;
    private Long journalEntryId;
    private Long cancellationJournalEntryId;
    private LocalDateTime approvedAt;
    private String approvedBy;
    private LocalDateTime cancelledAt;
    private String cancelledBy;
    private Instant createdAt;
    private Instant updatedAt;
    private List<SalesInvoiceLineDisplayDto> lines;
}
