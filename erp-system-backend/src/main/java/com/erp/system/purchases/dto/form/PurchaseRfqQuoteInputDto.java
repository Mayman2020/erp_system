package com.erp.system.purchases.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseRfqQuoteInputDto {

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long supplierId;

    @NotNull(message = "VALIDATION.REQUIRED")
    @DecimalMin(value = "0", message = "VALIDATION.NON_NEGATIVE")
    private BigDecimal unitPrice;

    private Integer leadDays;

    @Size(max = 300)
    private String notes;

    private Boolean selected;
}
