package com.erp.system.pos.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class PosShiftDiscrepancyTest {

    @Test
    void discrepancyIsClosingMinusExpected() {
        BigDecimal opening = new BigDecimal("100.00");
        BigDecimal cashSales = new BigDecimal("250.50");
        BigDecimal expected = opening.add(cashSales);
        BigDecimal closing = new BigDecimal("340.00");
        BigDecimal discrepancy = closing.subtract(expected);
        Assertions.assertEquals(new BigDecimal("350.50"), expected);
        Assertions.assertEquals(new BigDecimal("-10.50"), discrepancy);
    }
}
