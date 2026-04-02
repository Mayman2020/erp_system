package com.erp.system.accounting.controller;

import com.erp.system.accounting.dto.display.AccountDisplayDto;
import com.erp.system.accounting.dto.display.AccountTreeDisplayDto;
import com.erp.system.accounting.dto.form.AccountFormDto;
import com.erp.system.accounting.service.AccountService;
import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.enums.AccountingType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounting/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ApiResponse<List<AccountDisplayDto>> getAccounts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AccountingType type,
            @RequestParam(required = false) Boolean active
    ) {
        return ApiResponse.success(accountService.searchAccounts(search, type, active));
    }

    @GetMapping("/tree")
    public ApiResponse<List<AccountTreeDisplayDto>> getAccountTree() {
        return ApiResponse.success(accountService.getAccountTree());
    }

    @GetMapping("/{id}")
    public ApiResponse<AccountDisplayDto> getAccount(@PathVariable Long id) {
        return ApiResponse.success(accountService.getAccount(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AccountDisplayDto> createAccount(@Valid @RequestBody AccountFormDto request) {
        return ApiResponse.success(accountService.createAccount(request));
    }

    @PutMapping("/{accountId}")
    public ApiResponse<AccountDisplayDto> updateAccount(
            @PathVariable Long accountId,
            @Valid @RequestBody AccountFormDto request
    ) {
        return ApiResponse.success(accountService.updateAccount(accountId, request));
    }

    @PutMapping("/{accountId}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deactivateAccount(@PathVariable Long accountId) {
        accountService.deactivateAccount(accountId);
        return ApiResponse.success(null);
    }

    @PutMapping("/{accountId}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> activateAccount(@PathVariable Long accountId) {
        accountService.activateAccount(accountId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteAccount(@PathVariable Long accountId) {
        accountService.deleteAccount(accountId);
        return ApiResponse.success(null);
    }
}
