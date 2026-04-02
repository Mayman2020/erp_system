package com.erp.system.accounting.dto.form;

import com.erp.system.common.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ReceiptVoucherFormDto {

    @NotNull(message = "VALIDATION.REQUIRED")
    private LocalDate voucherDate;

    private String reference;

    private String description;

    @NotNull(message = "VALIDATION.REQUIRED")
    @DecimalMin(value = "0.01", message = "VALIDATION.AMOUNT_GT_ZERO")
    private BigDecimal amount;

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long cashAccountId;

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long revenueAccountId;

    @NotNull(message = "VALIDATION.REQUIRED")
    private PaymentMethod paymentMethod;

    @NotNull(message = "VALIDATION.REQUIRED")
    @Size(min = 3, max = 3, message = "VALIDATION.CURRENCY_CODE_LENGTH")
    private String currencyCode;

    @NotNull(message = "VALIDATION.REQUIRED")
    private String voucherType;

    private String partyName;

    private String invoiceReference;
}


