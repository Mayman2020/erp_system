package com.erp.system.partners.dto.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PartnerTransactionFormDto {

    @NotNull
    private Long partnerId;

    @NotBlank
    private String txnType;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private LocalDate txnDate;

    private String notes;
}
