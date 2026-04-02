package com.erp.system.accounting.domain;

import com.erp.system.common.entity.BaseEntity;
import com.erp.system.common.enums.ReconciliationLineSourceType;
import com.erp.system.common.enums.ReconciliationLineStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "reconciliation_lines", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconciliationLine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciliation_id", nullable = false)
    private Reconciliation reconciliation;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private ReconciliationLineSourceType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReconciliationLineStatus status = ReconciliationLineStatus.UNMATCHED;

    @Column(name = "source_reference", length = 80)
    private String sourceReference;

    @Column(name = "journal_entry_line_id")
    private Long journalEntryLineId;

    @Column(name = "matched_line_id")
    private Long matchedLineId;

    @Column(name = "matched_amount", precision = 19, scale = 2)
    private BigDecimal matchedAmount;

    @Column(name = "remaining_amount", precision = 19, scale = 2)
    private BigDecimal remainingAmount;

    @Version
    @Column(name = "version")
    private Long version;
}
