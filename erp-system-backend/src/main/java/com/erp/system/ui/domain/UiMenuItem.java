package com.erp.system.ui.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ui_menu_items", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
public class UiMenuItem {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "parent_id", length = 64)
    private String parentId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "item_type", nullable = false, length = 16)
    private String itemType;

    @Column(name = "title_key", nullable = false, length = 128)
    private String titleKey;

    @Column(length = 64)
    private String icon;

    @Column(length = 512)
    private String url;

    @Column(name = "is_external", nullable = false)
    private Boolean external = false;

    @Column(name = "target_blank", nullable = false)
    private Boolean targetBlank = false;

    @Column(name = "roles_csv", length = 256)
    private String rolesCsv;

    @Column(name = "item_classes", length = 128)
    private String itemClasses;

    @Column(name = "breadcrumbs_flag")
    private Boolean breadcrumbsFlag;
}
