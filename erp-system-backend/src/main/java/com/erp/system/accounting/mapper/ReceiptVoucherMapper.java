package com.erp.system.accounting.mapper;

import com.erp.system.accounting.domain.ReceiptVoucher;
import com.erp.system.accounting.dto.display.ReceiptVoucherDisplayDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReceiptVoucherMapper {

    @Mapping(target = "cashAccountId", source = "cashAccount.id")
    @Mapping(target = "cashAccountCode", source = "cashAccount.code")
    @Mapping(target = "cashAccountName", source = "cashAccount.nameEn")
    @Mapping(target = "cashAccountNameEn", source = "cashAccount.nameEn")
    @Mapping(target = "cashAccountNameAr", source = "cashAccount.nameAr")
    @Mapping(target = "revenueAccountId", source = "revenueAccount.id")
    @Mapping(target = "revenueAccountCode", source = "revenueAccount.code")
    @Mapping(target = "revenueAccountName", source = "revenueAccount.nameEn")
    @Mapping(target = "revenueAccountNameEn", source = "revenueAccount.nameEn")
    @Mapping(target = "revenueAccountNameAr", source = "revenueAccount.nameAr")
    @Mapping(target = "journalEntryId", source = "journalEntry.id")
    @Mapping(target = "reversalJournalEntryId", source = "reversalJournalEntry.id")
    ReceiptVoucherDisplayDto toDisplay(ReceiptVoucher voucher);
}
