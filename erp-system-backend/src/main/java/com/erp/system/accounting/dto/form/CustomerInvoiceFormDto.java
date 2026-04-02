package com.erp.system.accounting.dto.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CustomerInvoiceFormDto {

    private String invoiceNumber;

    @NotNull(message = "VALIDATION.REQUIRED")
    private LocalDate invoiceDate;

    @NotNull(message = "VALIDATION.REQUIRED")
    private LocalDate dueDate;

    private String customerName;

    private String customerReference;

    private String description;

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long receivableAccountId;

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long revenueAccountId;

    @NotNull(message = "VALIDATION.REQUIRED")
    @DecimalMin(value = "0.00", message = "VALIDATION.NON_NEGATIVE")
    private BigDecimal taxAmount;

    @Valid
    @NotEmpty(message = "VALIDATION.REQUIRED")
    private List<CustomerInvoiceLineFormDto> lines;
}


