package com.erp.system.purchases.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class PurchaseRfqDisplayDto {
    private Long id;
    private String rfqNo;
    private String title;
    private String status;
    private LocalDate dueDate;
    private String notes;
    private List<PurchaseRfqLineDisplayDto> lines;
    private List<PurchaseRfqQuoteDisplayDto> quotes;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
