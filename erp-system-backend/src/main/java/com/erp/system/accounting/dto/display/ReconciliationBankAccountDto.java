package com.erp.system.accounting.dto.display;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationBankAccountDto {
    private Long id;
    private String bankName;
    private String accountNumber;
    private String currency;
    private BigDecimal currentBalance;
}
