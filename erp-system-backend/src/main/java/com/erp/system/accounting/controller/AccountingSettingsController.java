package com.erp.system.accounting.controller;

import com.erp.system.accounting.dto.display.AccountingSettingsDisplayDto;
import com.erp.system.accounting.dto.display.FiscalPeriodDisplayDto;
import com.erp.system.accounting.dto.display.FiscalYearDisplayDto;
import com.erp.system.accounting.dto.form.AccountingSettingsUpdateDto;
import com.erp.system.accounting.dto.form.FiscalPeriodFormDto;
import com.erp.system.accounting.dto.form.FiscalYearFormDto;
import com.erp.system.accounting.service.AccountingAdministrationService;
import com.erp.system.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounting/settings")
@RequiredArgsConstructor
public class AccountingSettingsController {

    private final AccountingAdministrationService administrationService;

    @GetMapping
    public ApiResponse<AccountingSettingsDisplayDto> getSettings() {
        return ApiResponse.success(administrationService.getSettings());
    }

    @PutMapping
    public ApiResponse<AccountingSettingsDisplayDto> updateSettings(@Valid @RequestBody AccountingSettingsUpdateDto request) {
        return ApiResponse.success(administrationService.updateSettings(request));
    }

    @PostMapping("/fiscal-years")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FiscalYearDisplayDto> createFiscalYear(@Valid @RequestBody FiscalYearFormDto request) {
        return ApiResponse.success(administrationService.createFiscalYear(request));
    }

    @PostMapping("/fiscal-years/{id}/close")
    public ApiResponse<FiscalYearDisplayDto> closeFiscalYear(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(administrationService.closeFiscalYear(id, actor));
    }

    @PostMapping("/fiscal-years/{id}/open")
    public ApiResponse<FiscalYearDisplayDto> openFiscalYear(@PathVariable Long id) {
        return ApiResponse.success(administrationService.openFiscalYear(id));
    }

    @PostMapping("/fiscal-years/{id}/periods")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FiscalPeriodDisplayDto> createFiscalPeriod(@PathVariable Long id,
                                                                  @Valid @RequestBody FiscalPeriodFormDto request) {
        return ApiResponse.success(administrationService.createFiscalPeriod(id, request));
    }

    @PostMapping("/fiscal-periods/{id}/close")
    public ApiResponse<FiscalPeriodDisplayDto> closeFiscalPeriod(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(administrationService.closeFiscalPeriod(id, actor));
    }

    @PostMapping("/fiscal-periods/{id}/open")
    public ApiResponse<FiscalPeriodDisplayDto> openFiscalPeriod(@PathVariable Long id) {
        return ApiResponse.success(administrationService.openFiscalPeriod(id));
    }
}
