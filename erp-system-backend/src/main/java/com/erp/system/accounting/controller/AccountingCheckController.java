package com.erp.system.accounting.controller;

import com.erp.system.accounting.dto.display.AccountingCheckDisplayDto;
import com.erp.system.accounting.dto.form.AccountingCheckFormDto;
import com.erp.system.accounting.service.AccountingCheckService;
import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.enums.CheckStatus;
import com.erp.system.common.enums.CheckType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/accounting/checks")
@RequiredArgsConstructor
public class AccountingCheckController {

    private final AccountingCheckService checkService;

    @GetMapping
    public ApiResponse<List<AccountingCheckDisplayDto>> getChecks(
            @RequestParam(required = false) CheckType type,
            @RequestParam(required = false) CheckStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ApiResponse.success(checkService.getChecks(type, status, search, fromDate, toDate));
    }

    @GetMapping("/{id}")
    public ApiResponse<AccountingCheckDisplayDto> getCheck(@PathVariable Long id) {
        return ApiResponse.success(checkService.getCheck(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AccountingCheckDisplayDto> createCheck(@Valid @RequestBody AccountingCheckFormDto request) {
        return ApiResponse.success(checkService.createCheck(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AccountingCheckDisplayDto> updateCheck(@PathVariable Long id, @Valid @RequestBody AccountingCheckFormDto request) {
        return ApiResponse.success(checkService.updateCheck(id, request));
    }

    @PostMapping("/{id}/deposit")
    public ApiResponse<AccountingCheckDisplayDto> depositCheck(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(checkService.depositCheck(id, actor));
    }

    @PostMapping("/{id}/clear")
    public ApiResponse<AccountingCheckDisplayDto> clearCheck(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(checkService.clearCheck(id, actor));
    }

    @PostMapping("/{id}/bounce")
    public ApiResponse<AccountingCheckDisplayDto> bounceCheck(@PathVariable Long id,
                                                              @RequestParam String actor,
                                                              @RequestParam(required = false) String reason) {
        return ApiResponse.success(checkService.bounceCheck(id, actor, reason));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<AccountingCheckDisplayDto> cancelCheck(@PathVariable Long id,
                                                              @RequestParam String actor,
                                                              @RequestParam(required = false) String reason) {
        return ApiResponse.success(checkService.cancelCheck(id, actor, reason));
    }
}
