package com.erp.system.accounting.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "accounting_audit_log", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountingAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "action", nullable = false, length = 30)
    private String action;

    @Column(name = "actor", nullable = false, length = 100)
    private String actor;

    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;

    @Column(name = "performed_at", nullable = false)
    @Builder.Default
    private Instant performedAt = Instant.now();
}
