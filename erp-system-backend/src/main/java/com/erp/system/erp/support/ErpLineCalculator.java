package com.erp.system.erp.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ErpLineCalculator {

    private ErpLineCalculator() {
    }

    public static BigDecimal lineTotal(BigDecimal quantity, BigDecimal unitPrice, BigDecimal discountPercent, BigDecimal taxPercent) {
        BigDecimal qty = normalize(quantity);
        BigDecimal price = normalize(unitPrice);
        BigDecimal gross = qty.multiply(price).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = gross.multiply(normalizePercent(discountPercent)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal net = gross.subtract(discount);
        BigDecimal tax = net.multiply(normalizePercent(taxPercent)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return net.add(tax).setScale(2, RoundingMode.HALF_UP);
    }

    public static DocumentTotals documentTotals(java.util.List<BigDecimal> lineTotals, BigDecimal headerDiscount) {
        BigDecimal subtotal = lineTotals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = normalize(headerDiscount);
        BigDecimal total = subtotal.subtract(discount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        return new DocumentTotals(subtotal, discount, BigDecimal.ZERO, total);
    }

    public static DocumentTotals documentTotalsWithTax(BigDecimal subtotal, BigDecimal discount, BigDecimal tax) {
        return new DocumentTotals(
                normalize(subtotal),
                normalize(discount),
                normalize(tax),
                normalize(subtotal).subtract(normalize(discount)).add(normalize(tax)).setScale(2, RoundingMode.HALF_UP)
        );
    }

    private static BigDecimal normalize(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal normalizePercent(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public record DocumentTotals(BigDecimal subtotal, BigDecimal discountAmount, BigDecimal taxAmount, BigDecimal totalAmount) {
    }
}
