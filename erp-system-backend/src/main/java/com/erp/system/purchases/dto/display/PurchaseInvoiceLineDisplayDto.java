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
public class PurchaseInvoiceLineDisplayDto {
    private Long id;

private Long invoiceId;
private Long productId;
private String description;
private BigDecimal quantity;
private BigDecimal unitPrice;
private BigDecimal discountPercent;
private BigDecimal taxPercent;
private BigDecimal lineTotal;

    private Instant createdAt;
    private Instant updatedAt;
}
