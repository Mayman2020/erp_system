package com.erp.system.accounting.dto.display;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateDisplayDto {
    private Long id;
    private String sourceCurrency;
    private String targetCurrency;
    private BigDecimal rate;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
}
