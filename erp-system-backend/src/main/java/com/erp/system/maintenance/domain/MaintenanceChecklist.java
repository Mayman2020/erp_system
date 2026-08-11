package com.erp.system.maintenance.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "maintenance_checklists", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "item_text", nullable = false, length = 300)
    private String itemText;

    @Column(name = "is_done", nullable = false)
    @Builder.Default
    private boolean done = false;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;
}
