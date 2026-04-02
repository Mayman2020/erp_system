package com.erp.system.accounting.dto.form;

import com.erp.system.common.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AccountingTransactionFormDto {

    @NotNull(message = "VALIDATION.REQUIRED")
    private LocalDate transactionDate;

    private String reference;

    private String description;

    @NotNull(message = "VALIDATION.REQUIRED")
    private TransactionType transactionType;

    @NotNull(message = "VALIDATION.REQUIRED")
    @DecimalMin(value = "0.01", message = "VALIDATION.AMOUNT_GT_ZERO")
    private BigDecimal amount;

    private Long debitAccountId;

    private Long creditAccountId;

    private Long originalTransactionId;

    private String relatedDocumentReference;
}


