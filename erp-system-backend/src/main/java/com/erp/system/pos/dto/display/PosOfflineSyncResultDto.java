package com.erp.system.pos.dto.display;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PosOfflineSyncResultDto {
    private String batchKey;
    private String status;
    private int accepted;
    private int skipped;
    private List<PosSaleDisplayDto> sales;
}
