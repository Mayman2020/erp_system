package com.erp.system.accounting.controller;

import com.erp.system.accounting.dto.display.AccountingDashboardDisplayDto;
import com.erp.system.accounting.service.AccountingDashboardService;
import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.dto.PageResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/accounting/dashboard")
@RequiredArgsConstructor
public class AccountingDashboardController {

    private final AccountingDashboardService accountingDashboardService;

    @GetMapping
    public ApiResponse<AccountingDashboardDisplayDto> getDashboardOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ApiResponse.success(accountingDashboardService.getDashboard(fromDate, toDate));
    }

    @GetMapping("/financial-stats")
    public ApiResponse<AccountingDashboardDisplayDto> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ApiResponse.success(accountingDashboardService.getDashboard(fromDate, toDate));
    }

    @GetMapping("/recent-activity/{kind}")
    public ApiResponse<PageResultDto<AccountingDashboardDisplayDto.RecentDocument>> getRecentActivity(
            @PathVariable String kind,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        return ApiResponse.success(accountingDashboardService.getRecentActivity(kind, page, size, sortBy, sortDirection));
    }
}
