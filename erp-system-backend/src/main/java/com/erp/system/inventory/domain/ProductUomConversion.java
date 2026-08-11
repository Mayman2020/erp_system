package com.erp.system.inventory.domain;

import com.erp.system.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_uom_conversions", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUomConversion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private UnitOfMeasure unit;

    @Column(name = "factor_to_base", nullable = false, precision = 19, scale = 6)
    @Builder.Default
    private BigDecimal factorToBase = BigDecimal.ONE;

    @Column(name = "is_purchase", nullable = false)
    @Builder.Default
    private boolean purchase = false;

    @Column(name = "is_sales", nullable = false)
    @Builder.Default
    private boolean sales = false;
}
