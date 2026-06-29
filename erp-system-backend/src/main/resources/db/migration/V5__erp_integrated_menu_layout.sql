-- ERP integrated menu layout + manufacturing module entry.

UPDATE ui_menu_items SET title_key = 'MENU.FINANCE_ACCOUNTING' WHERE id = 'hesabaty';
UPDATE ui_menu_items SET title_key = 'DASHBOARD.TITLE', sort_order = 0 WHERE id = 'dashboard';

UPDATE ui_menu_items SET sort_order = 10 WHERE id = 'erp-sales';
UPDATE ui_menu_items SET sort_order = 20 WHERE id = 'erp-purchases';
UPDATE ui_menu_items SET sort_order = 30 WHERE id = 'erp-inventory';
UPDATE ui_menu_items SET sort_order = 50 WHERE id = 'erp-hr';
UPDATE ui_menu_items SET sort_order = 55 WHERE id = 'hesabaty';
UPDATE ui_menu_items SET sort_order = 60 WHERE id = 'erp-crm';
UPDATE ui_menu_items SET sort_order = 70 WHERE id = 'erp-projects';
UPDATE ui_menu_items SET sort_order = 80 WHERE id = 'erp-reports-erp';

INSERT INTO ui_menu_items (id, parent_id, sort_order, item_type, title_key, icon, url, is_external, target_blank, roles_csv, item_classes, breadcrumbs_flag)
VALUES
('erp-manufacturing', NULL, 40, 'group', 'MENU.MANUFACTURING', 'precision_manufacturing', NULL, FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER', NULL, FALSE),
('erp-manufacturing-orders', 'erp-manufacturing', 1, 'item', 'MENU.WORK_ORDERS', 'build', '/manufacturing', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER', NULL, TRUE)
ON CONFLICT (id) DO UPDATE SET
    parent_id = EXCLUDED.parent_id,
    sort_order = EXCLUDED.sort_order,
    item_type = EXCLUDED.item_type,
    title_key = EXCLUDED.title_key,
    icon = EXCLUDED.icon,
    url = EXCLUDED.url,
    roles_csv = EXCLUDED.roles_csv;

INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT r.id, m.id, TRUE, TRUE, TRUE, TRUE, 'system', 'system'
FROM access_roles r
CROSS JOIN ui_menu_items m
WHERE r.code IN ('ADMIN', 'MANAGER', 'ACCOUNTANT_STANDARD')
  AND m.id LIKE 'erp-manufacturing%'
  AND NOT EXISTS (
      SELECT 1 FROM role_menu_permissions rp
      WHERE rp.role_id = r.id AND rp.menu_item_id = m.id
  );
