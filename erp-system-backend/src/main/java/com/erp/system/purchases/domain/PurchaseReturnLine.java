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
@Table(name = "purchase_return_lines", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReturnLine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


@Column(name = "return_id", nullable = false)
private Long returnId;

@Column(name = "product_id", nullable = false)
private Long productId;

@Column(name = "quantity", nullable = false, precision = 19, scale = 4)
private BigDecimal quantity;

@Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
private BigDecimal unitPrice;

@Column(name = "line_total", nullable = false, precision = 19, scale = 2)
private BigDecimal lineTotal;

}
