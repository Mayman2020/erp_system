package com.erp.system.partners.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "profit_distribution_lines", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfitDistributionLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "distribution_id", nullable = false)
    private ProfitDistribution distribution;

    @Column(name = "partner_id", nullable = false)
    private Long partnerId;

    @Column(name = "share_percent", nullable = false, precision = 9, scale = 4)
    private BigDecimal sharePercent;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
}
