package com.erp.system.accounting.domain;

import com.erp.system.common.entity.BaseEntity;
import com.erp.system.common.enums.AccountingType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 30, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String legacyName;

    @Column(name = "name_en", nullable = false, length = 150)
    private String nameEn;

    @Column(name = "name_ar", length = 150)
    private String nameAr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Account parent;

    @Builder.Default
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Account> children = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountingType accountType;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "full_path", length = 500)
    private String fullPath;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Builder.Default
    @Column(name = "is_postable", nullable = false)
    private boolean postable = true;

    @Builder.Default
    @Column(name = "opening_balance", precision = 19, scale = 2)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "opening_balance_side", length = 10)
    private BalanceSide openingBalanceSide;

    @Version
    @Column(name = "version")
    private Long version;

    public enum BalanceSide {
        DEBIT, CREDIT
    }

    @PostLoad
    @PrePersist
    @PreUpdate
    private void synchronizeDerivedFields() {
        this.nameEn = firstNonBlank(nameEn, legacyName);
        this.legacyName = this.nameEn;
        this.nameAr = normalize(nameAr);

        if (parent != null) {
            String parentPath = parent.getFullPath();
            this.fullPath = (parentPath == null || parentPath.isBlank() ? this.nameEn : parentPath + "/" + this.nameEn);
        } else {
            this.fullPath = this.nameEn;
        }
    }

    private String firstNonBlank(String primary, String fallback) {
        String normalizedPrimary = normalize(primary);
        if (normalizedPrimary != null) {
            return normalizedPrimary;
        }
        return normalize(fallback);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public boolean isDebitBalance() {
        return accountType == AccountingType.ASSET || accountType == AccountingType.EXPENSE;
    }

    public boolean isCreditBalance() {
        return accountType == AccountingType.LIABILITY || accountType == AccountingType.EQUITY || accountType == AccountingType.INCOME;
    }

    public BalanceSide normalBalanceSide() {
        return isDebitBalance() ? BalanceSide.DEBIT : BalanceSide.CREDIT;
    }

    public BigDecimal signedOpeningBalance() {
        BigDecimal amount = openingBalance == null ? BigDecimal.ZERO : openingBalance;
        BalanceSide side = openingBalanceSide == null ? normalBalanceSide() : openingBalanceSide;
        return side == BalanceSide.DEBIT ? amount : amount.negate();
    }
}
