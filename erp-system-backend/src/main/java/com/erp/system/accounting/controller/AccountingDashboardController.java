package com.erp.system.accounting.controller;

import com.erp.system.accounting.dto.display.AccountingDashboardDisplayDto;
import com.erp.system.accounting.service.AccountingDashboardService;
import com.erp.system.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounting/dashboard")
@RequiredArgsConstructor
public class AccountingDashboardController {

    private final AccountingDashboardService accountingDashboardService;

    @GetMapping("/financial-stats")
    public ApiResponse<AccountingDashboardDisplayDto> getDashboard() {
        return ApiResponse.success(accountingDashboardService.getDashboard());
    }
}
