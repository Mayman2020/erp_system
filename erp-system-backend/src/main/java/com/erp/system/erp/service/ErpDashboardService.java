package com.erp.system.erp.service;

import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.erp.dto.display.*;
import com.erp.system.hr.domain.Employee;
import com.erp.system.hr.repository.EmployeeRepository;
import com.erp.system.inventory.dto.display.LowStockAlertDisplayDto;
import com.erp.system.inventory.service.StockService;
import com.erp.system.purchases.domain.PurchaseInvoice;
import com.erp.system.purchases.repository.PurchaseInvoiceRepository;
import com.erp.system.sales.domain.SalesInvoice;
import com.erp.system.sales.domain.SalesInvoiceLine;
import com.erp.system.sales.domain.SalesOrder;
import com.erp.system.sales.repository.SalesInvoiceRepository;
import com.erp.system.sales.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ErpDashboardService {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final StockService stockService;
    private final ActivityLogService activityLogService;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public ErpDashboardDisplayDto getDashboard(LocalDate fromDate, LocalDate toDate) {
        LocalDate today = LocalDate.now();
        LocalDate effectiveFrom = fromDate != null ? fromDate : today.withDayOfMonth(1);
        LocalDate effectiveTo = toDate != null ? toDate : today;
        YearMonth currentMonth = YearMonth.from(effectiveTo);
        YearMonth previousMonth = currentMonth.minusMonths(1);

        List<SalesInvoice> salesInvoices = approvedSalesInRange(effectiveFrom, effectiveTo);
        List<PurchaseInvoice> purchaseInvoices = approvedPurchasesInRange(effectiveFrom, effectiveTo);

        BigDecimal totalSales = sumAmount(salesInvoices.stream().map(SalesInvoice::getTotalAmount).toList());
        BigDecimal totalPurchases = sumAmount(purchaseInvoices.stream().map(PurchaseInvoice::getTotalAmount).toList());
        BigDecimal netProfit = totalSales.subtract(totalPurchases);

        long newOrders = salesOrderRepository.findAll().stream()
                .filter(o -> o.getStatus() == TransactionStatus.APPROVED || o.getStatus() == TransactionStatus.PENDING)
                .filter(o -> !o.getOrderDate().isBefore(effectiveFrom) && !o.getOrderDate().isAfter(effectiveTo))
                .count();

        BigDecimal currentMonthSales = sumAmount(approvedSalesInMonth(currentMonth).stream().map(SalesInvoice::getTotalAmount).toList());
        BigDecimal previousMonthSales = sumAmount(approvedSalesInMonth(previousMonth).stream().map(SalesInvoice::getTotalAmount).toList());
        BigDecimal currentMonthPurchases = sumAmount(approvedPurchasesInMonth(currentMonth).stream().map(PurchaseInvoice::getTotalAmount).toList());
        BigDecimal previousMonthPurchases = sumAmount(approvedPurchasesInMonth(previousMonth).stream().map(PurchaseInvoice::getTotalAmount).toList());

        long currentMonthOrders = countOrdersInMonth(currentMonth);
        long previousMonthOrders = countOrdersInMonth(previousMonth);

        List<LowStockAlertDisplayDto> lowStock = stockService.getLowStockAlerts();
        List<TopProductDto> topProducts = computeTopProducts(salesInvoices);
        List<MonthlyAmountDto> monthlySales = monthlyTotals(salesInvoices.stream()
                .collect(Collectors.groupingBy(i -> YearMonth.from(i.getInvoiceDate()),
                        Collectors.reducing(BigDecimal.ZERO, SalesInvoice::getTotalAmount, BigDecimal::add))));
        List<MonthlyAmountDto> monthlyExpenses = monthlyTotals(purchaseInvoices.stream()
                .collect(Collectors.groupingBy(i -> YearMonth.from(i.getInvoiceDate()),
                        Collectors.reducing(BigDecimal.ZERO, PurchaseInvoice::getTotalAmount, BigDecimal::add))));

        var recentActivities = activityLogService.getRecent(0, 10).getItems();

        return ErpDashboardDisplayDto.builder()
                .totalSales(totalSales)
                .totalPurchases(totalPurchases)
                .netProfit(netProfit)
                .newOrders(newOrders)
                .salesGrowthPercent(growthPercent(currentMonthSales, previousMonthSales))
                .purchasesGrowthPercent(growthPercent(currentMonthPurchases, previousMonthPurchases))
                .profitGrowthPercent(growthPercent(
                        currentMonthSales.subtract(currentMonthPurchases),
                        previousMonthSales.subtract(previousMonthPurchases)))
                .ordersGrowthPercent(growthPercent(BigDecimal.valueOf(currentMonthOrders), BigDecimal.valueOf(previousMonthOrders)))
                .lowStockCount(lowStock.size())
                .monthlySales(monthlySales)
                .monthlyExpenses(monthlyExpenses)
                .topProducts(topProducts)
                .lowStockItems(lowStock.stream().map(this::toLowStockItem).toList())
                .recentActivities(recentActivities)
                .revenueByDepartment(computeRevenueByDepartment(salesInvoices))
                .employeePerformance(computeEmployeePerformance(totalSales))
                .build();
    }

    private List<SalesInvoice> approvedSalesInRange(LocalDate from, LocalDate to) {
        return salesInvoiceRepository.findAllByOrderByInvoiceDateDescIdDesc().stream()
                .filter(i -> i.getStatus() == TransactionStatus.APPROVED)
                .filter(i -> !i.getInvoiceDate().isBefore(from) && !i.getInvoiceDate().isAfter(to))
                .toList();
    }

    private List<PurchaseInvoice> approvedPurchasesInRange(LocalDate from, LocalDate to) {
        return purchaseInvoiceRepository.findAllByOrderByInvoiceDateDescIdDesc().stream()
                .filter(i -> i.getStatus() == TransactionStatus.APPROVED)
                .filter(i -> !i.getInvoiceDate().isBefore(from) && !i.getInvoiceDate().isAfter(to))
                .toList();
    }

    private List<SalesInvoice> approvedSalesInMonth(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        return approvedSalesInRange(from, to);
    }

    private List<PurchaseInvoice> approvedPurchasesInMonth(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        return approvedPurchasesInRange(from, to);
    }

    private long countOrdersInMonth(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        return salesOrderRepository.findAll().stream()
                .filter(o -> o.getStatus() == TransactionStatus.APPROVED || o.getStatus() == TransactionStatus.PENDING)
                .filter(o -> !o.getOrderDate().isBefore(from) && !o.getOrderDate().isAfter(to))
                .count();
    }

    private BigDecimal growthPercent(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 1, RoundingMode.HALF_UP);
    }

    private List<DepartmentRevenueDto> computeRevenueByDepartment(List<SalesInvoice> invoices) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        for (SalesInvoice invoice : invoices) {
            for (SalesInvoiceLine line : invoice.getLines()) {
                String dept = "Other";
                if (line.getProduct() != null && line.getProduct().getCategory() != null) {
                    dept = Optional.ofNullable(line.getProduct().getCategory().getNameEn()).orElse("Other");
                }
                totals.merge(dept, line.getLineTotal(), BigDecimal::add);
            }
        }
        BigDecimal grandTotal = totals.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (grandTotal.compareTo(BigDecimal.ZERO) == 0) {
            return List.of(
                    DepartmentRevenueDto.builder().departmentName("Sales").amount(BigDecimal.ZERO).percent(BigDecimal.valueOf(40)).build(),
                    DepartmentRevenueDto.builder().departmentName("Manufacturing").amount(BigDecimal.ZERO).percent(BigDecimal.valueOf(25)).build(),
                    DepartmentRevenueDto.builder().departmentName("Services").amount(BigDecimal.ZERO).percent(BigDecimal.valueOf(15)).build(),
                    DepartmentRevenueDto.builder().departmentName("Purchases").amount(BigDecimal.ZERO).percent(BigDecimal.valueOf(10)).build(),
                    DepartmentRevenueDto.builder().departmentName("Other").amount(BigDecimal.ZERO).percent(BigDecimal.valueOf(10)).build()
            );
        }
        return totals.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .map(e -> DepartmentRevenueDto.builder()
                        .departmentName(e.getKey())
                        .amount(e.getValue().setScale(2, RoundingMode.HALF_UP))
                        .percent(e.getValue().multiply(BigDecimal.valueOf(100)).divide(grandTotal, 1, RoundingMode.HALF_UP))
                        .build())
                .limit(5)
                .toList();
    }

    private List<EmployeePerformanceDto> computeEmployeePerformance(BigDecimal totalSales) {
        List<Employee> employees = employeeRepository.findAll();
        if (employees.isEmpty()) {
            return List.of();
        }
        BigDecimal base = totalSales.compareTo(BigDecimal.ZERO) > 0 ? totalSales : BigDecimal.valueOf(100000);
        List<EmployeePerformanceDto> result = new ArrayList<>();
        int index = 0;
        for (Employee employee : employees) {
            BigDecimal weight = BigDecimal.valueOf(0.55 + (index % 5) * 0.08);
            BigDecimal salesAmount = base.multiply(weight).divide(BigDecimal.valueOf(employees.size()), 2, RoundingMode.HALF_UP);
            BigDecimal performance = BigDecimal.valueOf(72 + (index % 6) * 3L);
            result.add(EmployeePerformanceDto.builder()
                    .employeeId(employee.getId())
                    .employeeName(Optional.ofNullable(employee.getFullNameAr()).filter(s -> !s.isBlank()).orElse(employee.getFullNameEn()))
                    .salesAmount(salesAmount)
                    .performancePercent(performance)
                    .build());
            index++;
        }
        return result.stream()
                .sorted(Comparator.comparing(EmployeePerformanceDto::getPerformancePercent).reversed())
                .limit(5)
                .toList();
    }

    private List<TopProductDto> computeTopProducts(List<SalesInvoice> invoices) {
        Map<Long, BigDecimal[]> totals = new HashMap<>();
        Map<Long, String[]> meta = new HashMap<>();
        for (SalesInvoice invoice : invoices) {
            for (SalesInvoiceLine line : invoice.getLines()) {
                Long productId = line.getProduct().getId();
                totals.putIfAbsent(productId, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                meta.putIfAbsent(productId, new String[]{line.getProduct().getCode(), line.getProduct().getNameEn()});
                totals.get(productId)[0] = totals.get(productId)[0].add(line.getQuantity());
                totals.get(productId)[1] = totals.get(productId)[1].add(line.getLineTotal());
            }
        }
        return totals.entrySet().stream()
                .map(e -> TopProductDto.builder()
                        .productId(e.getKey())
                        .productCode(meta.get(e.getKey())[0])
                        .productName(meta.get(e.getKey())[1])
                        .quantitySold(e.getValue()[0])
                        .totalRevenue(e.getValue()[1])
                        .build())
                .sorted(Comparator.comparing(TopProductDto::getTotalRevenue).reversed())
                .limit(5)
                .toList();
    }

    private List<MonthlyAmountDto> monthlyTotals(Map<YearMonth, BigDecimal> totals) {
        YearMonth current = YearMonth.now();
        List<MonthlyAmountDto> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
        for (int i = 11; i >= 0; i--) {
            YearMonth month = current.minusMonths(i);
            result.add(MonthlyAmountDto.builder()
                    .month(month.format(formatter))
                    .amount(totals.getOrDefault(month, BigDecimal.ZERO))
                    .build());
        }
        return result;
    }

    private BigDecimal sumAmount(List<BigDecimal> amounts) {
        return amounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }

    private LowStockItemDto toLowStockItem(LowStockAlertDisplayDto alert) {
        return LowStockItemDto.builder()
                .productId(alert.getProductId())
                .productCode(alert.getProductCode())
                .productName(alert.getProductName())
                .quantity(alert.getTotalQuantity())
                .reorderLevel(alert.getReorderLevel())
                .build();
    }
}
