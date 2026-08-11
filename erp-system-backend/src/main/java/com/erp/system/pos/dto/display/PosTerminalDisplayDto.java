package com.erp.system.pos.dto.display;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PosTerminalDisplayDto {
    private Long id;
    private String code;
    private String name;
    private Long warehouseId;
    private String warehouseName;
    private boolean active;
}
