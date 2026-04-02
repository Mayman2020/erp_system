package com.erp.system.accounting.dto.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class JournalEntryFormDto {

    @NotNull(message = "VALIDATION.REQUIRED")
    private LocalDate entryDate;

    private String description;

    private String externalReference;

    @Size(min = 3, max = 3, message = "VALIDATION.CURRENCY_CODE_LENGTH")
    private String currencyCode;

    private String entryType;

    @Valid
    @NotEmpty(message = "VALIDATION.REQUIRED")
    private List<JournalEntryLineFormDto> lines;
}


