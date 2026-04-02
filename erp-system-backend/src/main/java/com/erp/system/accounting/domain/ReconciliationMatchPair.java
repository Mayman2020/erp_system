package com.erp.system.accounting.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "reconciliation_match_pairs", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconciliationMatchPair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciliation_id", nullable = false)
    private Reconciliation reconciliation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statement_line_id", nullable = false)
    private ReconciliationLine statementLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_line_id", nullable = false)
    private ReconciliationLine systemLine;

    @Column(name = "matched_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal matchedAmount;

    @Column(name = "matched_at", nullable = false)
    @Builder.Default
    private Instant matchedAt = Instant.now();

    @Column(name = "matched_by", nullable = false, length = 100)
    private String matchedBy;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "unmatched_at")
    private Instant unmatchedAt;

    @Column(name = "unmatched_by", length = 100)
    private String unmatchedBy;
}
