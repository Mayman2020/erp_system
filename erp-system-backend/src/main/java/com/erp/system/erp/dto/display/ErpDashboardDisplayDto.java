package com.erp.system.erp.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ErpDashboardDisplayDto {
    private BigDecimal totalSales;
    private BigDecimal totalPurchases;
    private BigDecimal netProfit;
    private long newOrders;
    private BigDecimal salesGrowthPercent;
    private BigDecimal purchasesGrowthPercent;
    private BigDecimal profitGrowthPercent;
    private long lowStockCount;
    private List<MonthlyAmountDto> monthlySales;
    private List<MonthlyAmountDto> monthlyExpenses;
    private List<TopProductDto> topProducts;
    private List<LowStockItemDto> lowStockItems;
    private List<ActivityLogDisplayDto> recentActivities;
    private List<DepartmentRevenueDto> revenueByDepartment;
    private List<EmployeePerformanceDto> employeePerformance;
    private BigDecimal ordersGrowthPercent;
}
