package com.erp.system.purchases.domain;

import com.erp.system.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "purchase_returns", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReturn extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


@Column(name = "return_number", nullable = false, length = 50, unique = true)
private String returnNumber;

@Column(name = "return_date", nullable = false)
private LocalDate returnDate;

@Column(name = "supplier_id", nullable = false)
private Long supplierId;

@Column(name = "invoice_id")
private Long invoiceId;

@Column(name = "warehouse_id")
private Long warehouseId;

@Enumerated(EnumType.STRING)
@Column(name = "status", nullable = false, length = 20)
@Builder.Default
private com.erp.system.common.enums.TransactionStatus status = com.erp.system.common.enums.TransactionStatus.DRAFT;

@Column(name = "subtotal", nullable = false, precision = 19, scale = 2)
@Builder.Default
private BigDecimal subtotal = BigDecimal.ZERO;

@Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
@Builder.Default
private BigDecimal taxAmount = BigDecimal.ZERO;

@Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
@Builder.Default
private BigDecimal totalAmount = BigDecimal.ZERO;

@Column(name = "notes", length = 500)
private String notes;

@Column(name = "journal_entry_id")
private Long journalEntryId;

}
