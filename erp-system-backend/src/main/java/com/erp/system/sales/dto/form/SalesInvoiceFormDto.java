package com.erp.system.sales.dto.form;

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
public class SalesInvoiceFormDto {

    private String invoiceNumber;

    @NotNull(message = "VALIDATION.REQUIRED")
    private LocalDate invoiceDate;

    @NotNull(message = "VALIDATION.REQUIRED")
    private LocalDate dueDate;

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long customerId;

    private Long orderId;

    private Long warehouseId;

    @DecimalMin(value = "0.00", message = "VALIDATION.NON_NEGATIVE")
    private BigDecimal discountAmount;

    @Size(max = 500)
    private String notes;

    @Valid
    @NotEmpty(message = "VALIDATION.REQUIRED")
    private List<SalesInvoiceLineFormDto> lines;
}
