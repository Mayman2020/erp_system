package com.erp.system.purchases.dto.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PurchaseRfqFormDto {

    private String rfqNo;

    @NotBlank(message = "VALIDATION.REQUIRED")
    @Size(max = 200)
    private String title;

    private LocalDate dueDate;

    @Size(max = 500)
    private String notes;

    @NotEmpty(message = "VALIDATION.REQUIRED")
    @Valid
    private List<PurchaseRfqLineInputDto> lines;
}
