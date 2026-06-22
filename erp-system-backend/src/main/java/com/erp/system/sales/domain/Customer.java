package com.erp.system.sales.domain;

import com.erp.system.accounting.domain.Account;
import com.erp.system.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "customers", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Column(name = "name_en", nullable = false, length = 200)
    private String nameEn;

    @Column(name = "name_ar", length = 200)
    private String nameAr;

    @Column(name = "email", length = 190)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "tax_number", length = 50)
    private String taxNumber;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "credit_limit", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receivable_account_id")
    private Account receivableAccount;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
