package com.erp.system.erp.service;

import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.inventory.dto.display.LowStockAlertDisplayDto;
import com.erp.system.inventory.dto.display.StockLevelDisplayDto;
import com.erp.system.inventory.service.StockService;
import com.erp.system.purchases.domain.PurchaseInvoice;
import com.erp.system.purchases.repository.PurchaseInvoiceRepository;
import com.erp.system.purchases.repository.SupplierRepository;
import com.erp.system.sales.domain.SalesInvoice;
import com.erp.system.sales.repository.SalesInvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ErpReportService {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final SupplierRepository supplierRepository;
    private final StockService stockService;

    @Transactional(readOnly = true)
    public Map<String, Object> salesReport(LocalDate fromDate, LocalDate toDate) {
        LocalDate from = fromDate != null ? fromDate : LocalDate.now().withDayOfMonth(1);
        LocalDate to = toDate != null ? toDate : LocalDate.now();
        List<SalesInvoice> invoices = salesInvoiceRepository.findAllByOrderByInvoiceDateDescIdDesc().stream()
                .filter(i -> i.getStatus() == TransactionStatus.APPROVED)
                .filter(i -> !i.getInvoiceDate().isBefore(from) && !i.getInvoiceDate().isAfter(to))
                .toList();
        BigDecimal total = invoices.stream().map(SalesInvoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        Map<String, Object> result = new HashMap<>();
        result.put("fromDate", from);
        result.put("toDate", to);
        result.put("invoiceCount", invoices.size());
        result.put("totalSales", total);
        result.put("invoices", invoices.stream().map(i -> Map.of(
                "number", i.getInvoiceNumber(),
                "date", i.getInvoiceDate(),
                "customer", i.getCustomer().getNameEn(),
                "total", i.getTotalAmount()
        )).toList());
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> purchasesReport(LocalDate fromDate, LocalDate toDate) {
        LocalDate from = fromDate != null ? fromDate : LocalDate.now().withDayOfMonth(1);
        LocalDate to = toDate != null ? toDate : LocalDate.now();
        List<PurchaseInvoice> invoices = purchaseInvoiceRepository.findAllByOrderByInvoiceDateDescIdDesc().stream()
                .filter(i -> i.getStatus() == TransactionStatus.APPROVED)
                .filter(i -> !i.getInvoiceDate().isBefore(from) && !i.getInvoiceDate().isAfter(to))
                .toList();
        BigDecimal total = invoices.stream().map(PurchaseInvoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        Map<String, Object> result = new HashMap<>();
        result.put("fromDate", from);
        result.put("toDate", to);
        result.put("invoiceCount", invoices.size());
        result.put("totalPurchases", total);
        result.put("invoices", invoices.stream().map(i -> {
            String supplierName = supplierRepository.findById(i.getSupplierId())
                    .map(s -> s.getNameEn())
                    .orElse("—");
            return Map.<String, Object>of(
                    "number", i.getInvoiceNumber(),
                    "date", i.getInvoiceDate(),
                    "supplier", supplierName,
                    "total", i.getTotalAmount()
            );
        }).toList());
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> inventoryReport() {
        List<StockLevelDisplayDto> levels = stockService.getStockLevels(null, null);
        List<LowStockAlertDisplayDto> lowStock = stockService.getLowStockAlerts();
        BigDecimal valuation = levels.stream()
                .map(l -> l.getQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> result = new HashMap<>();
        result.put("totalSkus", levels.size());
        result.put("lowStockCount", lowStock.size());
        result.put("totalQuantity", valuation);
        result.put("stockLevels", levels);
        result.put("lowStockAlerts", lowStock);
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> profitReport(LocalDate fromDate, LocalDate toDate) {
        Map<String, Object> sales = salesReport(fromDate, toDate);
        Map<String, Object> purchases = purchasesReport(fromDate, toDate);
        BigDecimal totalSales = (BigDecimal) sales.get("totalSales");
        BigDecimal totalPurchases = (BigDecimal) purchases.get("totalPurchases");
        Map<String, Object> result = new HashMap<>();
        result.put("fromDate", sales.get("fromDate"));
        result.put("toDate", sales.get("toDate"));
        result.put("totalSales", totalSales);
        result.put("totalPurchases", totalPurchases);
        result.put("netProfit", totalSales.subtract(totalPurchases));
        return result;
    }
}
