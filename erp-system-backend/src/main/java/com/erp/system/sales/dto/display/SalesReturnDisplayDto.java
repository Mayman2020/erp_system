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
public class SalesReturnDisplayDto {

    private Long id;
    private String returnNumber;
    private LocalDate returnDate;
    private Long customerId;
    private String customerCode;
    private String customerName;
    private Long invoiceId;
    private String invoiceNumber;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private TransactionStatus status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String notes;
    private Long journalEntryId;
    private Instant createdAt;
    private Instant updatedAt;
    private List<SalesReturnLineDisplayDto> lines;
}
