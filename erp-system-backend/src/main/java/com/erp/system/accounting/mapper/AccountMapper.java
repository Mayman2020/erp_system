package com.erp.system.accounting.mapper;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.dto.display.AccountDisplayDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentCode", source = "parent.code")
    AccountDisplayDto toDisplay(Account account);
}
