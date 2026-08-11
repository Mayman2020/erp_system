package com.erp.system.alerts.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "alert_events", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_id")
    private Long ruleId;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "severity", nullable = false, length = 20)
    @Builder.Default
    private String severity = "WARNING";

    @Column(name = "entity_type", length = 40)
    private String entityType;

    @Column(name = "entity_ref", length = 80)
    private String entityRef;

    @Column(name = "deep_link", length = 300)
    private String deepLink;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "NEW";

    @Column(name = "dedupe_key", length = 120, unique = true)
    private String dedupeKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
