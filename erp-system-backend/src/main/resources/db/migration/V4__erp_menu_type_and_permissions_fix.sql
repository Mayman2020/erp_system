-- ERP sidebar: lowercase menu types (frontend expects group/item), Material icons, broader role grants.

UPDATE ui_menu_items
SET item_type = LOWER(item_type)
WHERE item_type IS NOT NULL
  AND item_type <> LOWER(item_type);

UPDATE ui_menu_items SET icon = 'shopping_bag' WHERE id = 'erp-sales';
UPDATE ui_menu_items SET icon = 'groups' WHERE id = 'erp-sales-customers';
UPDATE ui_menu_items SET icon = 'description' WHERE id = 'erp-sales-quotations';
UPDATE ui_menu_items SET icon = 'shopping_cart' WHERE id = 'erp-sales-orders';
UPDATE ui_menu_items SET icon = 'receipt_long' WHERE id = 'erp-sales-invoices';
UPDATE ui_menu_items SET icon = 'undo' WHERE id = 'erp-sales-returns';

UPDATE ui_menu_items SET icon = 'local_shipping' WHERE id = 'erp-purchases';
UPDATE ui_menu_items SET icon = 'business' WHERE id = 'erp-purchases-suppliers';
UPDATE ui_menu_items SET icon = 'shopping_cart' WHERE id = 'erp-purchases-orders';
UPDATE ui_menu_items SET icon = 'request_quote' WHERE id = 'erp-purchases-invoices';
UPDATE ui_menu_items SET icon = 'undo' WHERE id = 'erp-purchases-returns';
UPDATE ui_menu_items SET icon = 'payments' WHERE id = 'erp-purchases-payments';

UPDATE ui_menu_items SET icon = 'inventory_2' WHERE id = 'erp-inventory';
UPDATE ui_menu_items SET icon = 'inventory' WHERE id = 'erp-inventory-products';
UPDATE ui_menu_items SET icon = 'category' WHERE id = 'erp-inventory-categories';
UPDATE ui_menu_items SET icon = 'warehouse' WHERE id = 'erp-inventory-warehouses';
UPDATE ui_menu_items SET icon = 'swap_horiz' WHERE id = 'erp-inventory-movements';
UPDATE ui_menu_items SET icon = 'bar_chart' WHERE id = 'erp-inventory-stock';

UPDATE ui_menu_items SET icon = 'badge' WHERE id = 'erp-hr';
UPDATE ui_menu_items SET icon = 'grid_view' WHERE id = 'erp-hr-departments';
UPDATE ui_menu_items SET icon = 'person' WHERE id = 'erp-hr-employees';
UPDATE ui_menu_items SET icon = 'schedule' WHERE id = 'erp-hr-attendance';
UPDATE ui_menu_items SET icon = 'event' WHERE id = 'erp-hr-leave';
UPDATE ui_menu_items SET icon = 'payments' WHERE id = 'erp-hr-payroll';

UPDATE ui_menu_items SET icon = 'contact_page' WHERE id = 'erp-crm';
UPDATE ui_menu_items SET icon = 'person_add' WHERE id = 'erp-crm-leads';
UPDATE ui_menu_items SET icon = 'timeline' WHERE id = 'erp-crm-activities';

UPDATE ui_menu_items SET icon = 'work' WHERE id = 'erp-projects';
UPDATE ui_menu_items SET icon = 'folder' WHERE id = 'erp-projects-list';

UPDATE ui_menu_items SET icon = 'insert_chart' WHERE id = 'erp-reports-erp';
UPDATE ui_menu_items SET icon = 'trending_up' WHERE id = 'erp-reports-sales';
UPDATE ui_menu_items SET icon = 'trending_down' WHERE id = 'erp-reports-purchases';
UPDATE ui_menu_items SET icon = 'inventory_2' WHERE id = 'erp-reports-inventory';
UPDATE ui_menu_items SET icon = 'paid' WHERE id = 'erp-reports-profit';

-- Grant ERP menu visibility to operational roles (not only ADMIN).
INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT r.id, m.id, TRUE, TRUE, TRUE, TRUE, 'system', 'system'
FROM access_roles r
CROSS JOIN ui_menu_items m
WHERE r.code IN ('MANAGER', 'ACCOUNTANT_STANDARD', 'SALES', 'PURCHASE', 'INVENTORY', 'HR', 'REPORT_VIEWER')
  AND m.id LIKE 'erp-%'
  AND NOT EXISTS (
      SELECT 1 FROM role_menu_permissions rp
      WHERE rp.role_id = r.id AND rp.menu_item_id = m.id
  );
