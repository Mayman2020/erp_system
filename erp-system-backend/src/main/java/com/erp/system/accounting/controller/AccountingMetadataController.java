package com.erp.system.accounting.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.enums.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/accounting/metadata")
public class AccountingMetadataController {

    @GetMapping
    public ApiResponse<AccountingMetadataResponse> getMetadata() {
        return ApiResponse.success(AccountingMetadataResponse.builder()
                .accountingTypes(enumValues(AccountingType.values()))
                .journalStatuses(enumValues(JournalEntryStatus.values()))
                .voucherStatuses(enumValues(VoucherStatus.values()))
                .transferStatuses(enumValues(TransferStatus.values()))
                .transactionTypes(enumValues(TransactionType.values()))
                .transactionStatuses(enumValues(TransactionStatus.values()))
                .billStatuses(enumValues(BillStatus.values()))
                .checkTypes(enumValues(CheckType.values()))
                .checkStatuses(enumValues(CheckStatus.values()))
                .paymentMethods(enumValues(PaymentMethod.values()))
                .reconciliationStatuses(enumValues(ReconciliationStatus.values()))
                .budgetStatuses(enumValues(BudgetStatus.values()))
                .build());
    }

    private List<String> enumValues(Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).toList();
    }

    @lombok.Builder
    @lombok.Value
    static class AccountingMetadataResponse {
        List<String> accountingTypes;
        List<String> journalStatuses;
        List<String> voucherStatuses;
        List<String> transferStatuses;
        List<String> transactionTypes;
        List<String> transactionStatuses;
        List<String> billStatuses;
        List<String> checkTypes;
        List<String> checkStatuses;
        List<String> paymentMethods;
        List<String> reconciliationStatuses;
        List<String> budgetStatuses;
    }
}
