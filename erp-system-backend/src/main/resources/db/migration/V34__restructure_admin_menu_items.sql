SET search_path TO erp_system, public;

DELETE FROM ui_menu_items
WHERE id = 'accountants';

INSERT INTO ui_menu_items (id, parent_id, sort_order, item_type, title_key, icon, url, is_external, target_blank, roles_csv, item_classes, breadcrumbs_flag)
VALUES
    ('admin-group', 'hesabaty', 14, 'collapse', 'NAV.SYSTEM_MANAGEMENT', 'admin_panel_settings', NULL, FALSE, FALSE, 'ADMIN', NULL, FALSE),
    ('admin-users', 'admin-group', 0, 'item', 'NAV.USERS', 'people', '/accountants/users', FALSE, FALSE, 'ADMIN', 'nav-item', FALSE),
    ('admin-roles', 'admin-group', 1, 'item', 'NAV.ROLES', 'shield', '/accountants/roles', FALSE, FALSE, 'ADMIN', 'nav-item', FALSE),
    ('admin-lookups', 'admin-group', 2, 'item', 'NAV.LOOKUPS', 'list_alt', '/accountants/lookups', FALSE, FALSE, 'ADMIN', 'nav-item', FALSE);

INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT role.id,
       menu.id,
       TRUE,
       TRUE,
       TRUE,
       TRUE,
       'flyway',
       'flyway'
FROM access_roles role
JOIN ui_menu_items menu ON menu.id IN ('admin-group', 'admin-users', 'admin-roles', 'admin-lookups')
WHERE role.code = 'ADMIN'
ON CONFLICT (role_id, menu_item_id) DO UPDATE
SET can_view   = EXCLUDED.can_view,
    can_create = EXCLUDED.can_create,
    can_edit   = EXCLUDED.can_edit,
    can_delete = EXCLUDED.can_delete,
    updated_by = 'flyway';
