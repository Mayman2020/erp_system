package com.erp.system.accounting.dto.display;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerDisplayDto {

    private Long accountId;
    private String accountCode;
    private String accountName;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private List<LedgerLineDisplayDto> lines;
}
