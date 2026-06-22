package com.erp.system.sales.support;

import com.erp.system.common.exception.BusinessException;
import com.erp.system.erp.support.ErpLineCalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class SalesDocumentTotalsSupport {

    private SalesDocumentTotalsSupport() {
    }

    public record LineAmounts(BigDecimal netAmount, BigDecimal taxAmount, BigDecimal lineTotal) {
    }

    public record DocumentAmounts(BigDecimal subtotal, BigDecimal taxAmount, BigDecimal totalAmount) {
    }

    public static LineAmounts calculateLineAmounts(BigDecimal quantity,
                                                   BigDecimal unitPrice,
                                                   BigDecimal discountPercent,
                                                   BigDecimal taxPercent) {
        BigDecimal qty = normalizePositive(quantity, "Quantity");
        BigDecimal price = normalizeAmount(unitPrice);
        BigDecimal gross = qty.multiply(price).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = gross.multiply(normalizePercent(discountPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal net = gross.subtract(discount);
        BigDecimal tax = net.multiply(normalizePercent(taxPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal lineTotal = ErpLineCalculator.lineTotal(qty, price, discountPercent, taxPercent);
        return new LineAmounts(net, tax, lineTotal);
    }

    public static DocumentAmounts calculateDocumentAmounts(BigDecimal lineNetSubtotal,
                                                           BigDecimal lineTaxTotal,
                                                           BigDecimal headerDiscount) {
        BigDecimal subtotal = normalizeAmount(lineNetSubtotal);
        BigDecimal taxAmount = normalizeAmount(lineTaxTotal);
        BigDecimal discount = normalizeAmount(headerDiscount);
        ErpLineCalculator.DocumentTotals totals = ErpLineCalculator.documentTotalsWithTax(subtotal, discount, taxAmount);
        return new DocumentAmounts(totals.subtotal(), totals.taxAmount(), totals.totalAmount());
    }

    public static BigDecimal calculateReturnLineTotal(BigDecimal quantity, BigDecimal unitPrice) {
        BigDecimal qty = normalizePositive(quantity, "Quantity");
        BigDecimal price = normalizeAmount(unitPrice);
        return qty.multiply(price).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Amount cannot be negative");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal normalizePositive(BigDecimal amount, String label) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(label + " must be greater than zero");
        }
        return amount.setScale(4, RoundingMode.HALF_UP);
    }

    private static BigDecimal normalizePercent(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Percentage cannot be negative");
        }
        return value;
    }
}
