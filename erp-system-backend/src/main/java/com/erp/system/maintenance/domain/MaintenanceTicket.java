package com.erp.system.maintenance.domain;

import com.erp.system.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_tickets", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceTicket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_no", nullable = false, length = 40, unique = true)
    private String ticketNo;

    @Column(name = "asset_id")
    private Long assetId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private String priority = "MEDIUM";

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "OPEN";

    @Column(name = "ticket_type", nullable = false, length = 20)
    @Builder.Default
    private String ticketType = "CORRECTIVE";

    @Column(name = "technician_id")
    private Long technicianId;

    @Column(name = "sla_hours")
    private Integer slaHours;

    @Column(name = "opened_at", nullable = false)
    @Builder.Default
    private LocalDateTime openedAt = LocalDateTime.now();

    @Column(name = "closed_at")
    private LocalDateTime closedAt;
}
