package com.erp.system.purchases.dto.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReturnFormDto {

    @Size(max = 50)
    private String returnNumber;

    @NotNull
    private LocalDate returnDate;

    @NotNull
    private Long supplierId;

    private Long invoiceId;
    private Long warehouseId;

    @DecimalMin("0.0")
    private BigDecimal taxAmount;

    @Size(max = 500)
    private String notes;

    @Valid
    @NotEmpty(message = "VALIDATION.REQUIRED")
    private List<PurchaseReturnLineInputDto> lines;
}
