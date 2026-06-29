package com.erp.system.purchases.dto.display;

import com.erp.system.common.enums.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReturnDisplayDto {
    private Long id;

private String returnNumber;
private LocalDate returnDate;
private Long supplierId;
private Long invoiceId;
private Long warehouseId;
private TransactionStatus status;
private BigDecimal subtotal;
private BigDecimal taxAmount;
private BigDecimal totalAmount;
private String notes;
private Long journalEntryId;
private List<PurchaseReturnLineDisplayDto> lines;

    private Instant createdAt;
    private Instant updatedAt;
}
