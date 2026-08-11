package com.erp.system.inventory.dto.display;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ProductBarcodeDisplayDto {
    private Long id;
    private Long productId;
    private String barcode;
    private boolean primaryBarcode;
    private Instant createdAt;
    private Instant updatedAt;
}
