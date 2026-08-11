package com.erp.system.purchases.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "purchase_rfq_quotes", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRfqQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rfq_id", nullable = false)
    private Long rfqId;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "lead_days", nullable = false)
    @Builder.Default
    private int leadDays = 0;

    @Column(name = "notes", length = 300)
    private String notes;

    @Column(name = "is_selected", nullable = false)
    @Builder.Default
    private boolean selected = false;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
