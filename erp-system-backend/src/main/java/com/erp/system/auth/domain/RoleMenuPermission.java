package com.erp.system.auth.domain;

import com.erp.system.common.entity.BaseEntity;
import com.erp.system.ui.domain.UiMenuItem;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "role_menu_permissions",
        schema = "erp_system",
        uniqueConstraints = @UniqueConstraint(name = "uq_role_menu_permissions", columnNames = {"role_id", "menu_item_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleMenuPermission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private AccessRole role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private UiMenuItem menuItem;

    @Builder.Default
    private boolean canView = false;

    @Builder.Default
    private boolean canCreate = false;

    @Builder.Default
    private boolean canEdit = false;

    @Builder.Default
    private boolean canDelete = false;
}
