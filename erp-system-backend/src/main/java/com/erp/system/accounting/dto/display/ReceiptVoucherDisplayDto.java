package com.erp.system.accounting.dto.display;

import com.erp.system.common.enums.PaymentMethod;
import com.erp.system.common.enums.VoucherStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptVoucherDisplayDto {

    private Long id;
    private LocalDate voucherDate;
    private String reference;
    private String description;
    private BigDecimal amount;
    private VoucherStatus status;
    private PaymentMethod paymentMethod;
    private String currencyCode;
    private String voucherType;
    private String partyName;
    private String invoiceReference;
    private Long cashAccountId;
    private String cashAccountCode;
    private String cashAccountName;
    private Long revenueAccountId;
    private String revenueAccountCode;
    private String revenueAccountName;
    private Long journalEntryId;
    private Long reversalJournalEntryId;
    private LocalDateTime approvedAt;
    private String approvedBy;
    private LocalDateTime postedAt;
    private String postedBy;
    private Instant createdAt;
    private Instant updatedAt;
}
