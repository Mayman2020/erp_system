package com.erp.system.purchases.dto.display;

import com.erp.system.common.enums.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierPaymentDisplayDto {
    private Long id;
    private String paymentNumber;
    private LocalDate paymentDate;
    private Long supplierId;
    private Long invoiceId;
    private BigDecimal amount;
    private String paymentMethod;
    private TransactionStatus status;
    private String notes;
    private Long journalEntryId;
    private Instant createdAt;
    private Instant updatedAt;
}
