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
public class SalesOrderDisplayDto {

    private Long id;
    private String orderNumber;
    private LocalDate orderDate;
    private Long customerId;
    private String customerCode;
    private String customerName;
    private Long quotationId;
    private String quotationNumber;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private TransactionStatus status;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
    private List<SalesOrderLineDisplayDto> lines;
}
