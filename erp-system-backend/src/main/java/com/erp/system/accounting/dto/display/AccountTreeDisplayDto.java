package com.erp.system.accounting.dto.display;

import com.erp.system.common.enums.AccountingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountTreeDisplayDto {

    private Long id;
    private String code;
    private String name;
    private String nameAr;
    private String nameEn;
    private AccountingType accountType;
    private Integer level;
    private boolean active;

    @Builder.Default
    private List<AccountTreeDisplayDto> children = new ArrayList<>();
}
