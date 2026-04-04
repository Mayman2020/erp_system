SET search_path TO erp_system, public;

INSERT INTO ui_menu_items (id, parent_id, sort_order, item_type, title_key, icon, url, is_external, target_blank, roles_csv, item_classes, breadcrumbs_flag)
VALUES ('admin-screens', 'admin-group', 3, 'item', 'NAV.SCREENS', 'view_quilt', '/accountants/screens', FALSE, FALSE, 'ADMIN', 'nav-item', FALSE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT role.id,
       'admin-screens',
       TRUE,
       TRUE,
       TRUE,
       TRUE,
       'flyway',
       'flyway'
FROM access_roles role
WHERE role.code = 'ADMIN'
ON CONFLICT (role_id, menu_item_id) DO UPDATE
SET can_view   = EXCLUDED.can_view,
    can_create = EXCLUDED.can_create,
    can_edit   = EXCLUDED.can_edit,
    can_delete = EXCLUDED.can_delete,
    updated_by = 'flyway';
