package com.erp.system.accounting.dto.display;

import com.erp.system.accounting.domain.Account;
import com.erp.system.common.enums.AccountingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDisplayDto {

    private Long id;
    private String code;
    private String name;
    private String nameAr;
    private String nameEn;
    private Long parentId;
    private String parentCode;
    private AccountingType accountType;
    private Integer level;
    private String fullPath;
    private boolean active;
    private BigDecimal openingBalance;
    private Account.BalanceSide openingBalanceSide;
    private String financialStatement;
}
