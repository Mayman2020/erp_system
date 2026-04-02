package com.erp.system.accounting.dto.form;

import com.erp.system.common.enums.AccountingMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountingSettingsUpdateDto {

    @NotNull(message = "VALIDATION.REQUIRED")
    private AccountingMethod accountingMethod;

    @NotBlank(message = "VALIDATION.REQUIRED")
    private String baseCurrency;

    @NotBlank(message = "VALIDATION.REQUIRED")
    private String allowedCurrencies;
}


