package com.erp.system.accounting.dto.display;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyPostedMovementDto(LocalDate day, BigDecimal debitTotal, BigDecimal creditTotal) {}
