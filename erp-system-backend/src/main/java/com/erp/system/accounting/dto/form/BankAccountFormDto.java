package com.erp.system.accounting.dto.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BankAccountFormDto {

    @NotBlank(message = "VALIDATION.REQUIRED")
    private String bankName;

    @NotBlank(message = "VALIDATION.REQUIRED")
    private String accountNumber;

    private String iban;

    @NotBlank(message = "VALIDATION.REQUIRED")
    @Pattern(regexp = "^[A-Z]{3}$", message = "VALIDATION.CURRENCY_CODE_LENGTH")
    private String currency;

    @NotNull(message = "VALIDATION.REQUIRED")
    private BigDecimal openingBalance;

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long linkedAccountId;

    @NotNull(message = "VALIDATION.REQUIRED")
    private Boolean active;
}


