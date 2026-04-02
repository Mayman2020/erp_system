package com.erp.system.accounting.dto.form;

import com.erp.system.common.enums.CheckType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AccountingCheckFormDto {

    private String checkNumber;

    @NotNull(message = "VALIDATION.REQUIRED")
    private CheckType checkType;

    @NotNull(message = "VALIDATION.REQUIRED")
    private LocalDate issueDate;

    @NotNull(message = "VALIDATION.REQUIRED")
    private LocalDate dueDate;

    @NotNull(message = "VALIDATION.REQUIRED")
    private String bankName;

    @NotNull(message = "VALIDATION.REQUIRED")
    @DecimalMin(value = "0.01", message = "VALIDATION.AMOUNT_GT_ZERO")
    private BigDecimal amount;

    private String partyName;

    private String linkedDocumentReference;

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long bankAccountId;

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long holdingAccountId;
}


