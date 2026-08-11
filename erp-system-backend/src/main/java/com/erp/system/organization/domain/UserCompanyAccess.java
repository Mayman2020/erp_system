package com.erp.system.organization.domain;

import com.erp.system.auth.domain.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "user_company_access", schema = "erp_system")
@IdClass(UserCompanyAccessId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserCompanyAccess {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    @Column(name = "is_default", nullable = false)
    private boolean defaultCompany;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
