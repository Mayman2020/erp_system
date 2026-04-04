package com.erp.system.accounting.dto.display;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountDisplayDto {

    private Long id;
    private String bankName;
    private String accountNumber;
    private String iban;
    private String currency;
    private BigDecimal openingBalance;
    private BigDecimal currentBalance;
    private boolean active;
    private Long linkedAccountId;
    private String linkedAccountCode;
    private String linkedAccountName;
    private String linkedAccountNameEn;
    private String linkedAccountNameAr;
    private Instant createdAt;
    private Instant updatedAt;
}
