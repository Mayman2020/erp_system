package com.erp.system.pos.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class PosSaleDisplayDto {
    private Long id;
    private String saleNo;
    private Long shiftId;
    private Long warehouseId;
    private Long customerId;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private BigDecimal paidCash;
    private BigDecimal paidCard;
    private BigDecimal paidCredit;
    private String idempotencyKey;
    private Instant createdAt;
    private List<PosSaleLineDisplayDto> lines;
}
