package com.erp.system.purchases.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseInvoiceFormDto {
    @Size(max = 50)
    private String invoiceNumber;

    @NotNull
    private LocalDate invoiceDate;

    @NotNull
    private LocalDate dueDate;

    @NotNull
    private Long supplierId;

    private Long orderId;
    private Long warehouseId;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal subtotal;

    @DecimalMin("0.0")
    private BigDecimal discountAmount;

    @DecimalMin("0.0")
    private BigDecimal taxAmount;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal totalAmount;

    @Size(max = 500)
    private String notes;
}
