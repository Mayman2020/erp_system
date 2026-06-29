-- Business gaps: project expense posting link, transfers recovery, and missing ERP menus.
SET search_path TO erp_system, public;

ALTER TABLE project_expenses
    ADD COLUMN IF NOT EXISTS journal_entry_id BIGINT;

ALTER TABLE transfers
    ADD COLUMN IF NOT EXISTS journal_entry_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_project_expenses_journal_entry'
          AND conrelid = 'erp_system.project_expenses'::regclass
    ) THEN
        ALTER TABLE project_expenses
            ADD CONSTRAINT fk_project_expenses_journal_entry
                FOREIGN KEY (journal_entry_id) REFERENCES journal_entries (id);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_transfers_journal_entry'
          AND conrelid = 'erp_system.transfers'::regclass
    ) THEN
        ALTER TABLE transfers
            ADD CONSTRAINT fk_transfers_journal_entry
                FOREIGN KEY (journal_entry_id) REFERENCES journal_entries (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_project_expenses_journal_entry ON project_expenses (journal_entry_id);
CREATE INDEX IF NOT EXISTS idx_transfers_journal_entry ON transfers (journal_entry_id);

INSERT INTO ui_menu_items (id, parent_id, sort_order, item_type, title_key, icon, url, is_external, target_blank, roles_csv, item_classes, breadcrumbs_flag)
VALUES
    ('erp-manufacturing-bom', 'erp-manufacturing', 2, 'item', 'MENU.BOM', 'account_tree', '/manufacturing/bom', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER', NULL, TRUE),
    ('erp-inventory-low-stock', 'erp-inventory', 7, 'item', 'MENU.LOW_STOCK', 'warning', '/inventory/low-stock', FALSE, FALSE, 'ADMIN,MANAGER,INVENTORY,ACCOUNTANT_STANDARD', NULL, TRUE),
    ('erp-hr-documents', 'erp-hr', 6, 'item', 'MENU.EMPLOYEE_DOCUMENTS', 'folder_shared', '/hr/documents', FALSE, FALSE, 'ADMIN,MANAGER,HR', NULL, TRUE),
    ('erp-activity-log', 'dashboard', 2, 'item', 'MENU.ACTIVITY_LOG', 'history', '/erp/activity-log', FALSE, FALSE, 'ADMIN,ACCOUNTANT,ACCOUNTANT_STANDARD,MANAGER,REPORT_VIEWER', NULL, TRUE),
    ('transfers', 'hesabaty', 5, 'item', 'NAV.TRANSFERS', 'swap_horiz', '/transfers', FALSE, FALSE, 'ADMIN,ACCOUNTANT,ACCOUNTANT_STANDARD,MANAGER', 'nav-item', FALSE)
ON CONFLICT (id) DO UPDATE SET
    parent_id = EXCLUDED.parent_id,
    sort_order = EXCLUDED.sort_order,
    item_type = EXCLUDED.item_type,
    title_key = EXCLUDED.title_key,
    icon = EXCLUDED.icon,
    url = EXCLUDED.url,
    is_external = EXCLUDED.is_external,
    target_blank = EXCLUDED.target_blank,
    roles_csv = EXCLUDED.roles_csv,
    item_classes = EXCLUDED.item_classes,
    breadcrumbs_flag = EXCLUDED.breadcrumbs_flag;

INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT r.id, m.id, TRUE, TRUE, TRUE, TRUE, 'flyway', 'flyway'
FROM access_roles r
CROSS JOIN ui_menu_items m
WHERE r.code IN ('ADMIN', 'MANAGER', 'ACCOUNTANT_STANDARD')
  AND m.id = 'erp-manufacturing-bom'
ON CONFLICT (role_id, menu_item_id) DO UPDATE SET
    can_view = EXCLUDED.can_view,
    can_create = EXCLUDED.can_create,
    can_edit = EXCLUDED.can_edit,
    can_delete = EXCLUDED.can_delete,
    updated_by = 'flyway',
    updated_at = NOW();

INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT r.id, m.id, TRUE, TRUE, TRUE, TRUE, 'flyway', 'flyway'
FROM access_roles r
CROSS JOIN ui_menu_items m
WHERE r.code IN ('ADMIN', 'ACCOUNTANT', 'ACCOUNTANT_STANDARD', 'MANAGER')
  AND m.id = 'transfers'
ON CONFLICT (role_id, menu_item_id) DO UPDATE SET
    can_view = EXCLUDED.can_view,
    can_create = EXCLUDED.can_create,
    can_edit = EXCLUDED.can_edit,
    can_delete = EXCLUDED.can_delete,
    updated_by = 'flyway',
    updated_at = NOW();

INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT r.id, m.id, TRUE, TRUE, TRUE, TRUE, 'flyway', 'flyway'
FROM access_roles r
CROSS JOIN ui_menu_items m
WHERE r.code IN ('ADMIN', 'MANAGER', 'HR')
  AND m.id = 'erp-hr-documents'
ON CONFLICT (role_id, menu_item_id) DO UPDATE SET
    can_view = EXCLUDED.can_view,
    can_create = EXCLUDED.can_create,
    can_edit = EXCLUDED.can_edit,
    can_delete = EXCLUDED.can_delete,
    updated_by = 'flyway',
    updated_at = NOW();

INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT r.id, m.id, TRUE, FALSE, FALSE, FALSE, 'flyway', 'flyway'
FROM access_roles r
CROSS JOIN ui_menu_items m
WHERE r.code IN ('ADMIN', 'MANAGER', 'INVENTORY', 'ACCOUNTANT_STANDARD')
  AND m.id = 'erp-inventory-low-stock'
ON CONFLICT (role_id, menu_item_id) DO UPDATE SET
    can_view = EXCLUDED.can_view,
    can_create = EXCLUDED.can_create,
    can_edit = EXCLUDED.can_edit,
    can_delete = EXCLUDED.can_delete,
    updated_by = 'flyway',
    updated_at = NOW();

INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT r.id, m.id, TRUE, FALSE, FALSE, FALSE, 'flyway', 'flyway'
FROM access_roles r
CROSS JOIN ui_menu_items m
WHERE r.code IN ('ADMIN', 'ACCOUNTANT', 'ACCOUNTANT_STANDARD', 'MANAGER', 'REPORT_VIEWER')
  AND m.id = 'erp-activity-log'
ON CONFLICT (role_id, menu_item_id) DO UPDATE SET
    can_view = EXCLUDED.can_view,
    can_create = EXCLUDED.can_create,
    can_edit = EXCLUDED.can_edit,
    can_delete = EXCLUDED.can_delete,
    updated_by = 'flyway',
    updated_at = NOW();
