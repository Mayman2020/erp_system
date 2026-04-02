package com.erp.system.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lookup_types", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LookupType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 60, unique = true)
    private String code;

    @Column(name = "name_en", nullable = false, length = 150)
    private String nameEn;

    @Column(name = "name_ar", length = 150)
    private String nameAr;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
