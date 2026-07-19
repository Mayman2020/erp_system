SET search_path TO erp_system, public;

-- Force password change on next login (set by an admin when creating/resetting a user)
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

-- Global screen visibility toggles, independent of per-role ui_menu_items permissions
CREATE TABLE IF NOT EXISTS screen_settings (
    screen_key  VARCHAR(80) PRIMARY KEY,
    enabled     BOOLEAN NOT NULL DEFAULT TRUE,
    updated_by  VARCHAR(100),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Move the admin/security nav items to their new /admin/* routes (old /accountants/* paths
-- now redirect client-side; this keeps the menu seed pointing at the routes users actually land on)
UPDATE ui_menu_items SET url = '/admin/users' WHERE id = 'admin-users';
UPDATE ui_menu_items SET url = '/admin/roles' WHERE id = 'admin-roles';
UPDATE ui_menu_items SET url = '/admin/lookups' WHERE id = 'admin-lookups';
UPDATE ui_menu_items SET url = '/admin/screens' WHERE id = 'admin-screens';

INSERT INTO ui_menu_items (id, parent_id, sort_order, item_type, title_key, icon, url, is_external, target_blank, roles_csv, item_classes, breadcrumbs_flag)
VALUES ('admin-user-access', 'admin-group', 4, 'item', 'NAV.USER_ACCESS', 'admin_panel_settings', '/admin/user-access', FALSE, FALSE, 'ADMIN', 'nav-item', FALSE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT role.id,
       'admin-user-access',
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
