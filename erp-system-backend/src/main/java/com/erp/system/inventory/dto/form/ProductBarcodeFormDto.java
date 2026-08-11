package com.erp.system.inventory.dto.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductBarcodeFormDto {

    @NotBlank(message = "VALIDATION.REQUIRED")
    @Size(max = 80)
    private String barcode;

    private Boolean primaryBarcode;
}
