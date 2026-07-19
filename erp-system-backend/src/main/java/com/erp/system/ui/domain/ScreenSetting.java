package com.erp.system.ui.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "screen_settings", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
public class ScreenSetting {

    @Id
    @Column(name = "screen_key", length = 80)
    private String screenKey;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
