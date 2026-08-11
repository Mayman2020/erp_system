package com.erp.system.partners.dto.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PartnerFormDto {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotNull
    private BigDecimal sharePercent;

    private Long capitalAccountId;
    private Long drawingAccountId;
    private Boolean active;
}
