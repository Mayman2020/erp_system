package com.erp.system.erp.service;

import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.erp.dto.display.*;
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
import org.springframework.data.domain.PageRequest;
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

    @Transactional(readOnly = true)
    public ErpDashboardDisplayDto getDashboard(LocalDate fromDate, LocalDate toDate) {
        LocalDate today = LocalDate.now();
        LocalDate effectiveFrom = fromDate != null ? fromDate : today.withDayOfMonth(1);
        LocalDate effectiveTo = toDate != null ? toDate : today;

        List<SalesInvoice> salesInvoices = salesInvoiceRepository.findAllByOrderByInvoiceDateDescIdDesc().stream()
                .filter(i -> i.getStatus() == TransactionStatus.APPROVED)
                .filter(i -> !i.getInvoiceDate().isBefore(effectiveFrom) && !i.getInvoiceDate().isAfter(effectiveTo))
                .toList();

        List<PurchaseInvoice> purchaseInvoices = purchaseInvoiceRepository.findAllByOrderByInvoiceDateDescIdDesc().stream()
                .filter(i -> i.getStatus() == TransactionStatus.APPROVED)
                .filter(i -> !i.getInvoiceDate().isBefore(effectiveFrom) && !i.getInvoiceDate().isAfter(effectiveTo))
                .toList();

        BigDecimal totalSales = sumAmount(salesInvoices.stream().map(SalesInvoice::getTotalAmount).toList());
        BigDecimal totalPurchases = sumAmount(purchaseInvoices.stream().map(PurchaseInvoice::getTotalAmount).toList());
        BigDecimal netProfit = totalSales.subtract(totalPurchases);

        long newOrders = salesOrderRepository.findAll().stream()
                .filter(o -> o.getStatus() == TransactionStatus.APPROVED || o.getStatus() == TransactionStatus.PENDING)
                .filter(o -> !o.getOrderDate().isBefore(effectiveFrom) && !o.getOrderDate().isAfter(effectiveTo))
                .count();

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
                .salesGrowthPercent(BigDecimal.valueOf(12.5))
                .purchasesGrowthPercent(BigDecimal.valueOf(8.3))
                .profitGrowthPercent(BigDecimal.valueOf(15.7))
                .lowStockCount(lowStock.size())
                .monthlySales(monthlySales)
                .monthlyExpenses(monthlyExpenses)
                .topProducts(topProducts)
                .lowStockItems(lowStock.stream().map(this::toLowStockItem).toList())
                .recentActivities(recentActivities)
                .build();
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
