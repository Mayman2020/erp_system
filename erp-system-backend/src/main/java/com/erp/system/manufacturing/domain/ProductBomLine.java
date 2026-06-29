package com.erp.system.manufacturing.domain;

import com.erp.system.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_bom_lines", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductBomLine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_product_id", nullable = false)
    private Long parentProductId;

    @Column(name = "component_product_id", nullable = false)
    private Long componentProductId;

    @Column(name = "quantity_per_unit", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityPerUnit;
}
