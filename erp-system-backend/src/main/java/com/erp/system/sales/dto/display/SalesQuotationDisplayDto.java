package com.erp.system.sales.dto.display;

import com.erp.system.common.enums.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class SalesQuotationDisplayDto {

    private Long id;
    private String quotationNumber;
    private LocalDate quotationDate;
    private LocalDate validUntil;
    private Long customerId;
    private String customerCode;
    private String customerName;
    private TransactionStatus status;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
    private List<SalesQuotationLineDisplayDto> lines;
}
