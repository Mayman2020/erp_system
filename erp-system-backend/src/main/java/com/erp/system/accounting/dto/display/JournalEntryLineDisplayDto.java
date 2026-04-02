package com.erp.system.accounting.dto.display;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryLineDisplayDto {

    private Long id;
    private Long accountId;
    private String accountCode;
    private String accountNameEn;
    private String accountNameAr;
    private String description;
    private BigDecimal debit;
    private BigDecimal credit;
    private Integer lineNumber;
}
