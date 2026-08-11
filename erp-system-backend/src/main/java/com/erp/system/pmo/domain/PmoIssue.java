package com.erp.system.pmo.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pmo_issues", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PmoIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "OPEN";

    @Column(name = "owner_name", length = 120)
    private String ownerName;

    @Column(name = "notes", length = 500)
    private String notes;
}
