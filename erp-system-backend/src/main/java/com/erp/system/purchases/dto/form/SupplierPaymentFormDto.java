package com.erp.system.purchases.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class SupplierPaymentFormDto {
    @Size(max = 50)
    private String paymentNumber;

    @NotNull
    private LocalDate paymentDate;

    @NotNull
    private Long supplierId;

    private Long invoiceId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotBlank
    @Size(max = 30)
    private String paymentMethod;

    @Size(max = 500)
    private String notes;
}
