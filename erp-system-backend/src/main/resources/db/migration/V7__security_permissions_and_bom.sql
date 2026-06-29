-- Security permissions fix, missing menu items, payroll journal link, product BOM.

-- REPORT_VIEWER: view-only on ERP operational menus (not full CRUD).
UPDATE role_menu_permissions rp
SET can_create = FALSE,
    can_edit = FALSE,
    can_delete = FALSE,
    updated_by = 'flyway',
    updated_at = NOW()
FROM access_roles r,
     ui_menu_items m
WHERE rp.role_id = r.id
  AND rp.menu_item_id = m.id
  AND r.code = 'REPORT_VIEWER'
  AND m.id LIKE 'erp-%'
  AND m.id NOT LIKE 'erp-reports%';

-- Ensure REPORT_VIEWER can view ERP report menus.
INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT r.id, m.id, TRUE, FALSE, FALSE, FALSE, 'flyway', 'flyway'
FROM access_roles r
CROSS JOIN ui_menu_items m
WHERE r.code = 'REPORT_VIEWER'
  AND m.id LIKE 'erp-reports%'
  AND NOT EXISTS (
      SELECT 1 FROM role_menu_permissions rp
      WHERE rp.role_id = r.id AND rp.menu_item_id = m.id
  );

-- Payroll: link posted journal entry.
ALTER TABLE payroll_runs
    ADD COLUMN IF NOT EXISTS journal_entry_id BIGINT REFERENCES journal_entries (id);

-- Product BOM (bill of materials) for manufacturing consumption.
CREATE TABLE IF NOT EXISTS product_bom_lines (
    id BIGSERIAL PRIMARY KEY,
    parent_product_id BIGINT NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    component_product_id BIGINT NOT NULL REFERENCES products (id),
    quantity_per_unit NUMERIC(19, 4) NOT NULL CHECK (quantity_per_unit > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_product_bom_component UNIQUE (parent_product_id, component_product_id)
);

CREATE INDEX IF NOT EXISTS idx_product_bom_parent ON product_bom_lines (parent_product_id);

-- Hide transfers menu (no API/UI); use journal entries for inter-account moves.
UPDATE ui_menu_items
SET roles_csv = 'ADMIN',
    url = '/journal-entries',
    title_key = 'NAV.JOURNAL_ENTRIES'
WHERE id = 'transfers';

-- Accounting pages missing from hesabaty sidebar.
INSERT INTO ui_menu_items (id, parent_id, sort_order, item_type, title_key, icon, url, is_external, target_blank, roles_csv, item_classes, breadcrumbs_flag)
VALUES
('bills', 'hesabaty', 14, 'item', 'BILLS.TITLE', 'description', '/bills', FALSE, FALSE, 'ADMIN,ACCOUNTANT,ACCOUNTANT_STANDARD,MANAGER', 'nav-item', FALSE),
('budget', 'hesabaty', 15, 'item', 'BUDGET.TITLE', 'pie_chart', '/budget', FALSE, FALSE, 'ADMIN,ACCOUNTANT,ACCOUNTANT_STANDARD,MANAGER', 'nav-item', FALSE),
('exchange-rates', 'hesabaty', 16, 'item', 'EXCHANGE_RATES.TITLE', 'currency_exchange', '/exchange-rates', FALSE, FALSE, 'ADMIN,ACCOUNTANT,ACCOUNTANT_STANDARD,MANAGER', 'nav-item', FALSE),
('erp-inventory-units', 'erp-inventory', 6, 'item', 'MENU.UNITS', 'straighten', '/inventory/units', FALSE, FALSE, 'ADMIN,MANAGER,INVENTORY,ACCOUNTANT_STANDARD', NULL, TRUE),
('erp-crm-notes', 'erp-crm', 4, 'item', 'MENU.CRM_NOTES', 'sticky_note_2', '/crm/notes', FALSE, FALSE, 'ADMIN,MANAGER,SALES,ACCOUNTANT_STANDARD', NULL, TRUE)
ON CONFLICT (id) DO UPDATE SET
    parent_id = EXCLUDED.parent_id,
    sort_order = EXCLUDED.sort_order,
    title_key = EXCLUDED.title_key,
    icon = EXCLUDED.icon,
    url = EXCLUDED.url,
    roles_csv = EXCLUDED.roles_csv;

INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT r.id, m.id, TRUE, TRUE, TRUE, TRUE, 'flyway', 'flyway'
FROM access_roles r
CROSS JOIN ui_menu_items m
WHERE r.code IN ('ADMIN', 'ACCOUNTANT', 'ACCOUNTANT_STANDARD', 'MANAGER')
  AND m.id IN ('bills', 'budget', 'exchange-rates')
  AND NOT EXISTS (
      SELECT 1 FROM role_menu_permissions rp
      WHERE rp.role_id = r.id AND rp.menu_item_id = m.id
  );

INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT r.id, m.id, TRUE, TRUE, TRUE, TRUE, 'flyway', 'flyway'
FROM access_roles r
CROSS JOIN ui_menu_items m
WHERE r.code IN ('ADMIN', 'MANAGER', 'INVENTORY', 'ACCOUNTANT_STANDARD')
  AND m.id = 'erp-inventory-units'
  AND NOT EXISTS (
      SELECT 1 FROM role_menu_permissions rp
      WHERE rp.role_id = r.id AND rp.menu_item_id = m.id
  );

INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT r.id, m.id, TRUE, TRUE, TRUE, TRUE, 'flyway', 'flyway'
FROM access_roles r
CROSS JOIN ui_menu_items m
WHERE r.code IN ('ADMIN', 'MANAGER', 'SALES', 'ACCOUNTANT_STANDARD')
  AND m.id = 'erp-crm-notes'
  AND NOT EXISTS (
      SELECT 1 FROM role_menu_permissions rp
      WHERE rp.role_id = r.id AND rp.menu_item_id = m.id
  );
