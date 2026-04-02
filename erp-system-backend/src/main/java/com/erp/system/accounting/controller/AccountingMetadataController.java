package com.erp.system.accounting.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.dto.LookupItemDto;
import com.erp.system.common.service.LookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/accounting/metadata")
@RequiredArgsConstructor
public class AccountingMetadataController {

    private final LookupService lookupService;

    @GetMapping
    public ApiResponse<AccountingMetadataResponse> getMetadata() {
        return ApiResponse.success(AccountingMetadataResponse.builder()
                .accountingTypes(codeList("account-types"))
                .journalStatuses(codeList("journal-entry-statuses"))
                .voucherStatuses(codeList("voucher-statuses"))
                .transferStatuses(codeList("transfer-statuses"))
                .transactionTypes(codeList("transaction-types"))
                .transactionStatuses(codeList("transaction-statuses"))
                .billStatuses(codeList("bill-statuses"))
                .checkTypes(codeList("check-types"))
                .checkStatuses(codeList("check-statuses"))
                .paymentMethods(codeList("payment-methods"))
                .reconciliationStatuses(codeList("reconciliation-statuses"))
                .budgetStatuses(codeList("budget-statuses"))
                .build());
    }

    private List<String> codeList(String type) {
        return lookupService.getLookups(type).stream().map(LookupItemDto::getCode).toList();
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
