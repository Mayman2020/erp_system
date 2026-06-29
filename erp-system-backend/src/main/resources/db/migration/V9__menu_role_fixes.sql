-- Menu role fixes: INVENTORY access to BOM, align erp-reports URLs.
SET search_path TO erp_system, public;

UPDATE ui_menu_items
SET roles_csv = 'ADMIN,ACCOUNTANT_STANDARD,MANAGER,INVENTORY'
WHERE id = 'erp-manufacturing-bom';

INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT r.id, m.id, TRUE, TRUE, TRUE, TRUE, 'flyway', 'flyway'
FROM access_roles r
CROSS JOIN ui_menu_items m
WHERE r.code = 'INVENTORY'
  AND m.id = 'erp-manufacturing-bom'
ON CONFLICT (role_id, menu_item_id) DO UPDATE SET
    can_view = EXCLUDED.can_view,
    can_create = EXCLUDED.can_create,
    can_edit = EXCLUDED.can_edit,
    can_delete = EXCLUDED.can_delete,
    updated_by = 'flyway',
    updated_at = NOW();
