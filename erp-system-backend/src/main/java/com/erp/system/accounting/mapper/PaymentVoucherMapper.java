package com.erp.system.accounting.mapper;

import com.erp.system.accounting.domain.PaymentVoucher;
import com.erp.system.accounting.dto.display.PaymentVoucherDisplayDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentVoucherMapper {

    @Mapping(target = "cashAccountId", source = "cashAccount.id")
    @Mapping(target = "cashAccountCode", source = "cashAccount.code")
    @Mapping(target = "cashAccountName", source = "cashAccount.nameEn")
    @Mapping(target = "cashAccountNameEn", source = "cashAccount.nameEn")
    @Mapping(target = "cashAccountNameAr", source = "cashAccount.nameAr")
    @Mapping(target = "expenseAccountId", source = "expenseAccount.id")
    @Mapping(target = "expenseAccountCode", source = "expenseAccount.code")
    @Mapping(target = "expenseAccountName", source = "expenseAccount.nameEn")
    @Mapping(target = "expenseAccountNameEn", source = "expenseAccount.nameEn")
    @Mapping(target = "expenseAccountNameAr", source = "expenseAccount.nameAr")
    @Mapping(target = "journalEntryId", source = "journalEntry.id")
    @Mapping(target = "reversalJournalEntryId", source = "reversalJournalEntry.id")
    PaymentVoucherDisplayDto toDisplay(PaymentVoucher voucher);
}
