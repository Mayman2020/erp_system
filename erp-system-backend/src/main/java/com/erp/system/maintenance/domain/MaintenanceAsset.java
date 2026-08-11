package com.erp.system.maintenance.domain;

import com.erp.system.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "maintenance_assets", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceAsset extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_code", nullable = false, length = 40, unique = true)
    private String assetCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "serial_no", length = 80)
    private String serialNo;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "notes", length = 500)
    private String notes;
}
