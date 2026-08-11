package com.erp.system.pos.domain;

import com.erp.system.auth.domain.User;
import com.erp.system.common.entity.BaseEntity;
import com.erp.system.inventory.domain.Warehouse;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "pos_shifts", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosShift extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shift_no", nullable = false, length = 40, unique = true)
    private String shiftNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "terminal_id", nullable = false)
    private PosTerminal terminal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cashier_user_id", nullable = false)
    private User cashier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "OPEN";

    @Column(name = "opening_cash", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal openingCash = BigDecimal.ZERO;

    @Column(name = "closing_cash", precision = 19, scale = 2)
    private BigDecimal closingCash;

    @Column(name = "expected_cash", precision = 19, scale = 2)
    private BigDecimal expectedCash;

    @Column(name = "cash_sales", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal cashSales = BigDecimal.ZERO;

    @Column(name = "card_sales", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal cardSales = BigDecimal.ZERO;

    @Column(name = "credit_sales", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal creditSales = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal discrepancy;

    @Column(length = 500)
    private String notes;

    @Column(name = "opened_at", nullable = false)
    @Builder.Default
    private Instant openedAt = Instant.now();

    @Column(name = "closed_at")
    private Instant closedAt;
}
