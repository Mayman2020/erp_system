package com.erp.system.notification.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_user_id", nullable = false)
    private Long recipientUserId;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80)
    private NotificationType type;

    @Column(name = "title_key", nullable = false, length = 200)
    private String titleKey;

    @Column(name = "body_key", nullable = false, length = 200)
    private String bodyKey;

    @Column(name = "vars_json", columnDefinition = "TEXT")
    private String varsJson;

    @Column(name = "reference_type", length = 80)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
