package com.erp.system.accounting.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CustomerInvoiceLineDisplayDto {

    private Long id;
    private Long accountId;
    private String accountCode;
    private String accountName;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
