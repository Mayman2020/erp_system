package com.erp.system.erp.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.dto.PageResultDto;
import com.erp.system.erp.dto.display.ActivityLogDisplayDto;
import com.erp.system.erp.dto.display.ErpDashboardDisplayDto;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.erp.service.ErpDashboardService;
import com.erp.system.erp.service.ErpReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/erp")
@RequiredArgsConstructor
public class ErpDashboardController {

    private final ErpDashboardService dashboardService;
    private final ActivityLogService activityLogService;
    private final ErpReportService reportService;

    @GetMapping("/dashboard")
    public ApiResponse<ErpDashboardDisplayDto> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ApiResponse.success(dashboardService.getDashboard(fromDate, toDate));
    }

    @GetMapping("/activity-logs")
    public ApiResponse<PageResultDto<ActivityLogDisplayDto>> getActivityLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(activityLogService.getRecent(page, size));
    }

    @GetMapping("/reports/sales")
    public ApiResponse<Map<String, Object>> salesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ApiResponse.success(reportService.salesReport(fromDate, toDate));
    }

    @GetMapping("/reports/purchases")
    public ApiResponse<Map<String, Object>> purchasesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ApiResponse.success(reportService.purchasesReport(fromDate, toDate));
    }

    @GetMapping("/reports/inventory")
    public ApiResponse<Map<String, Object>> inventoryReport() {
        return ApiResponse.success(reportService.inventoryReport());
    }

    @GetMapping("/reports/profit")
    public ApiResponse<Map<String, Object>> profitReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ApiResponse.success(reportService.profitReport(fromDate, toDate));
    }
}
