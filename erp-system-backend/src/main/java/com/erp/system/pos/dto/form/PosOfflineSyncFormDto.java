package com.erp.system.pos.dto.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PosOfflineSyncFormDto {
    @NotBlank
    private String batchKey;
    private Long terminalId;
    @NotEmpty
    @Valid
    private List<PosSaleFormDto> sales = new ArrayList<>();
}
