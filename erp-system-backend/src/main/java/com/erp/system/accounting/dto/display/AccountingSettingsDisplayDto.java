package com.erp.system.accounting.dto.display;

import com.erp.system.common.enums.AccountingMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountingSettingsDisplayDto {

    private AccountingMethod accountingMethod;
    private String baseCurrency;
    private String allowedCurrencies;
    private List<NumberingSequenceDisplayDto> sequences;
    private List<FiscalYearDisplayDto> fiscalYears;
}
