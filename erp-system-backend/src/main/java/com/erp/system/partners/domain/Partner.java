package com.erp.system.partners.domain;

import com.erp.system.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "partners", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 40, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "share_percent", nullable = false, precision = 9, scale = 4)
    @Builder.Default
    private BigDecimal sharePercent = BigDecimal.ZERO;

    @Column(name = "capital_account_id")
    private Long capitalAccountId;

    @Column(name = "drawing_account_id")
    private Long drawingAccountId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
