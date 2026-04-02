package com.erp.system.accounting.mapper;

import com.erp.system.accounting.domain.JournalEntryLine;
import com.erp.system.accounting.dto.display.JournalEntryLineDisplayDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JournalEntryLineMapper {

    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "accountCode", source = "account.code")
    @Mapping(target = "accountNameEn", source = "account.nameEn")
    @Mapping(target = "accountNameAr", source = "account.nameAr")
    JournalEntryLineDisplayDto toDisplay(JournalEntryLine line);
}
