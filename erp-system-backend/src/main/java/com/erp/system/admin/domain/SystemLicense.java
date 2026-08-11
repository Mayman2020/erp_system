package com.erp.system.admin.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "system_licenses", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_key", nullable = false, length = 120, unique = true)
    private String licenseKey;

    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @Column(name = "modules_csv", length = 500)
    private String modulesCsv;

    @Column(name = "max_users", nullable = false)
    @Builder.Default
    private Integer maxUsers = 10;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;

    @Column(name = "grace_days", nullable = false)
    @Builder.Default
    private Integer graceDays = 7;

    @Column(name = "signature", nullable = false, columnDefinition = "TEXT")
    private String signature;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
