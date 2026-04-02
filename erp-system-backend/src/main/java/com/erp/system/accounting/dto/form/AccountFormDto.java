package com.erp.system.accounting.dto.form;

import com.erp.system.accounting.domain.Account;
import com.erp.system.common.enums.AccountingType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountFormDto {

    private String code;

    @NotBlank(message = "VALIDATION.REQUIRED")
    private String nameEn;

    private String nameAr;

    private Long parentId;

    @NotNull(message = "VALIDATION.REQUIRED")
    private AccountingType accountType;

    private Boolean active = true;

    private Boolean postable = true;

    @DecimalMin(value = "0", message = "VALIDATION.NON_NEGATIVE")
    @DecimalMax(value = "999999999.99", message = "VALIDATION.MAX_VALUE")
    private BigDecimal openingBalance = BigDecimal.ZERO;

    private Account.BalanceSide openingBalanceSide;
}


