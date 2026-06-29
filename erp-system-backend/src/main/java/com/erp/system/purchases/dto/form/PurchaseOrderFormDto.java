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
public class PurchaseOrderFormDto {

    @Size(max = 50)
    private String orderNumber;

    @NotNull
    private LocalDate orderDate;

    @NotNull
    private Long supplierId;

    private Long warehouseId;

    @DecimalMin("0.0")
    private BigDecimal discountAmount;

    @Size(max = 500)
    private String notes;

    @Valid
    @NotEmpty(message = "VALIDATION.REQUIRED")
    private List<PurchaseOrderLineInputDto> lines;
}
