package com.erp.system.accounting.controller;

import com.erp.system.accounting.dto.display.AccountingTransactionDisplayDto;
import com.erp.system.accounting.dto.form.AccountingTransactionFormDto;
import com.erp.system.accounting.service.AccountingTransactionService;
import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.common.enums.TransactionType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounting/transactions")
@RequiredArgsConstructor
public class AccountingTransactionController {

    private final AccountingTransactionService transactionService;

    @GetMapping
    public ApiResponse<List<AccountingTransactionDisplayDto>> getTransactions(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(transactionService.getTransactions(type, status, search));
    }

    @GetMapping("/{id}")
    public ApiResponse<AccountingTransactionDisplayDto> getTransaction(@PathVariable Long id) {
        return ApiResponse.success(transactionService.getTransaction(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AccountingTransactionDisplayDto> createTransaction(@Valid @RequestBody AccountingTransactionFormDto request) {
        return ApiResponse.success(transactionService.createTransaction(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AccountingTransactionDisplayDto> updateTransaction(@PathVariable Long id,
                                                                          @Valid @RequestBody AccountingTransactionFormDto request) {
        return ApiResponse.success(transactionService.updateTransaction(id, request));
    }

    @PostMapping("/{id}/post")
    public ApiResponse<AccountingTransactionDisplayDto> postTransaction(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(transactionService.postTransaction(id, actor));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<AccountingTransactionDisplayDto> cancelTransaction(@PathVariable Long id,
                                                                          @RequestParam String actor,
                                                                          @RequestParam(required = false) String reason) {
        return ApiResponse.success(transactionService.cancelTransaction(id, actor, reason));
    }
}
