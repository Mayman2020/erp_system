package com.erp.system.accounting.controller;

import com.erp.system.accounting.dto.display.BalanceSheetReportDto;
import com.erp.system.accounting.dto.display.ProfitLossReportDto;
import com.erp.system.accounting.service.AccountingReportService;
import com.erp.system.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/accounting/reports")
@RequiredArgsConstructor
public class AccountingReportController {

    private final AccountingReportService accountingReportService;

    @GetMapping("/profit-loss")
    public ApiResponse<ProfitLossReportDto> getProfitLoss(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String currency
    ) {
        return ApiResponse.success(accountingReportService.getProfitLoss(fromDate, toDate, currency));
    }

    @GetMapping("/balance-sheet")
    public ApiResponse<BalanceSheetReportDto> getBalanceSheet(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate,
            @RequestParam(required = false) String currency
    ) {
        return ApiResponse.success(accountingReportService.getBalanceSheet(asOfDate, currency));
    }
}
