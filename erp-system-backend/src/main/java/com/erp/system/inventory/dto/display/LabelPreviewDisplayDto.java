package com.erp.system.inventory.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LabelPreviewDisplayDto {
    private Long productId;
    private String barcode;
    private String qrPayload;
    private String name;
    private BigDecimal price;
}
