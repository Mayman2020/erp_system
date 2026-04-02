package com.erp.system.accounting.dto.form;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class JournalEntryLineFormDto {

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long accountId;

    private String description;

    @NotNull(message = "VALIDATION.REQUIRED")
    @DecimalMin(value = "0.0000", message = "VALIDATION.NON_NEGATIVE")
    private BigDecimal debit;

    @NotNull(message = "VALIDATION.REQUIRED")
    @DecimalMin(value = "0.0000", message = "VALIDATION.NON_NEGATIVE")
    private BigDecimal credit;

    @AssertTrue(message = "VALIDATION.DEBIT_CREDIT_EXCLUSIVE")
    public boolean isValidDebitCredit() {
        boolean hasDebit = debit != null && debit.compareTo(BigDecimal.ZERO) > 0;
        boolean hasCredit = credit != null && credit.compareTo(BigDecimal.ZERO) > 0;
        return hasDebit ^ hasCredit;
    }
}


