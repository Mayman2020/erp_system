package com.erp.system.inventory.domain;

import com.erp.system.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "units_of_measure", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitOfMeasure extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 20, unique = true)
    private String code;

    @Column(name = "name_en", nullable = false, length = 80)
    private String nameEn;

    @Column(name = "name_ar", length = 80)
    private String nameAr;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
