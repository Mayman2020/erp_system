package com.erp.system.accounting.controller;

import com.erp.system.accounting.dto.display.BankAccountDisplayDto;
import com.erp.system.accounting.dto.form.BankAccountFormDto;
import com.erp.system.accounting.service.BankAccountService;
import com.erp.system.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounting/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @GetMapping
    public ApiResponse<List<BankAccountDisplayDto>> getBankAccounts(@RequestParam(required = false) Boolean active,
                                                                    @RequestParam(required = false) String search) {
        return ApiResponse.success(bankAccountService.getBankAccounts(active, search));
    }

    @GetMapping("/{id}")
    public ApiResponse<BankAccountDisplayDto> getBankAccount(@PathVariable Long id) {
        return ApiResponse.success(bankAccountService.getBankAccount(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BankAccountDisplayDto> createBankAccount(@Valid @RequestBody BankAccountFormDto request) {
        return ApiResponse.success(bankAccountService.createBankAccount(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<BankAccountDisplayDto> updateBankAccount(@PathVariable Long id, @Valid @RequestBody BankAccountFormDto request) {
        return ApiResponse.success(bankAccountService.updateBankAccount(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteBankAccount(@PathVariable Long id) {
        bankAccountService.deleteBankAccount(id);
        return ApiResponse.success(null);
    }
}
