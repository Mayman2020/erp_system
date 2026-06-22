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
@Table(name = "purchase_order_lines", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderLine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


@Column(name = "order_id", nullable = false)
private Long orderId;

@Column(name = "product_id", nullable = false)
private Long productId;

@Column(name = "description", length = 500)
private String description;

@Column(name = "quantity", nullable = false, precision = 19, scale = 4)
private BigDecimal quantity;

@Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
private BigDecimal unitPrice;

@Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
@Builder.Default
private BigDecimal discountPercent = BigDecimal.ZERO;

@Column(name = "tax_percent", nullable = false, precision = 5, scale = 2)
@Builder.Default
private BigDecimal taxPercent = BigDecimal.ZERO;

@Column(name = "line_total", nullable = false, precision = 19, scale = 2)
@Builder.Default
private BigDecimal lineTotal = BigDecimal.ZERO;

}
