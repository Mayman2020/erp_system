package com.erp.system.accounting.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferFormDto {

    @NotNull
    private LocalDate transferDate;

    @Size(max = 80)
    private String reference;

    @Size(max = 500)
    private String description;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private Long sourceAccountId;

    @NotNull
    private Long destinationAccountId;
}
