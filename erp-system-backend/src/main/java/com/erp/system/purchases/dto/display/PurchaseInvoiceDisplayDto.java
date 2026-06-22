package com.erp.system.purchases.dto.display;

import com.erp.system.common.enums.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseInvoiceDisplayDto {
    private Long id;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private Long supplierId;
    private Long orderId;
    private Long warehouseId;
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
    private List<PurchaseInvoiceLineDisplayDto> lines;
    private Instant createdAt;
    private Instant updatedAt;
}
