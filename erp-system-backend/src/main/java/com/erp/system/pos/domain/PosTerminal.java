package com.erp.system.pos.domain;

import com.erp.system.common.entity.BaseEntity;
import com.erp.system.inventory.domain.Warehouse;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pos_terminals", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosTerminal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40, unique = true)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
