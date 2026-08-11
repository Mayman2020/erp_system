package com.erp.system.pos.dto.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class PosSaleFormDto {
    @NotNull
    private Long shiftId;
    private Long customerId;
    private String paymentMethod = "CASH";
    @DecimalMin("0")
    private BigDecimal discountAmount = BigDecimal.ZERO;
    @DecimalMin("0")
    private BigDecimal paidCash = BigDecimal.ZERO;
    @DecimalMin("0")
    private BigDecimal paidCard = BigDecimal.ZERO;
    @DecimalMin("0")
    private BigDecimal paidCredit = BigDecimal.ZERO;
    private String idempotencyKey;
    private String offlineBatchId;
    @NotEmpty
    @Valid
    private List<PosSaleLineFormDto> lines = new ArrayList<>();
}
