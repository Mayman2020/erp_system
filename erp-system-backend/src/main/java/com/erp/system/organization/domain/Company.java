package com.erp.system.organization.domain;

import com.erp.system.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "companies", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "name_en", nullable = false, length = 190)
    private String nameEn;

    @Column(name = "name_ar", nullable = false, length = 190)
    private String nameAr;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    private CompanyEntityType entityType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Company parent;

    @Column(name = "tax_id", length = 80)
    private String taxId;
    @Column(name = "registration_no", length = 80)
    private String registrationNo;
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;
    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;
    private String email;
    private String phone;
    private String address;
    @Column(name = "logo_url")
    private String logoUrl;
    @Column(name = "is_active", nullable = false)
    private boolean active;
}
