package com.erp.system.accounting.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExchangeRateFormDto {

    @NotNull
    @Size(min = 3, max = 3)
    private String sourceCurrency;

    @NotNull
    @Size(min = 3, max = 3)
    private String targetCurrency;

    @NotNull
    @DecimalMin(value = "0.000001")
    private BigDecimal rate;

    @NotNull
    private LocalDate effectiveDate;

    private LocalDate expiryDate;
}
