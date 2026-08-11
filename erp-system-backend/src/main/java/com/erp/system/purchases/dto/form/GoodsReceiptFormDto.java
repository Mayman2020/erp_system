package com.erp.system.purchases.dto.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class GoodsReceiptFormDto {

    private String receiptNo;

    private Long supplierId;

    @NotNull(message = "VALIDATION.REQUIRED")
    private Long warehouseId;

    private Long purchaseOrderId;

    @Size(max = 500)
    private String notes;

    @NotEmpty(message = "VALIDATION.REQUIRED")
    @Valid
    private List<GoodsReceiptLineInputDto> lines;
}
