SET search_path TO erp_system, public;

DELETE FROM erp_system.ui_menu_items
WHERE id IN ('dashboard', 'accounting', 'authentication', 'utilities', 'other');

INSERT INTO erp_system.ui_menu_items (id, parent_id, sort_order, item_type, title_key, icon, url, is_external, target_blank, roles_csv, item_classes, breadcrumbs_flag)
VALUES
    ('hesabaty', NULL, 0, 'group', 'NAV.HESABATY', 'apartment', NULL, FALSE, FALSE, 'ADMIN,ACCOUNTANT', NULL, NULL),
    ('dashboard', 'hesabaty', 0, 'item', 'NAV.DASHBOARD', 'space_dashboard', '/dashboard', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('accounts', 'hesabaty', 1, 'item', 'NAV.CHART_OF_ACCOUNTS', 'account_tree', '/accounts', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('journal-entries', 'hesabaty', 2, 'item', 'NAV.JOURNAL_ENTRIES', 'menu_book', '/journal-entries', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('payment-vouchers', 'hesabaty', 3, 'item', 'NAV.PAYMENT_VOUCHERS', 'payments', '/payment-vouchers', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('receipt-vouchers', 'hesabaty', 4, 'item', 'NAV.RECEIPT_VOUCHERS', 'receipt_long', '/receipt-vouchers', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('transfers', 'hesabaty', 5, 'item', 'NAV.TRANSFERS', 'swap_horiz', '/transfers', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('transactions', 'hesabaty', 6, 'item', 'NAV.TRANSACTIONS', 'sync_alt', '/transactions', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('invoices', 'hesabaty', 7, 'item', 'NAV.INVOICES', 'request_quote', '/invoices', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('checks', 'hesabaty', 8, 'item', 'NAV.CHECKS', 'rule', '/checks', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('bank-accounts', 'hesabaty', 9, 'item', 'NAV.BANK_ACCOUNTS', 'account_balance', '/bank-accounts', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('ledger', 'hesabaty', 10, 'item', 'NAV.LEDGER', 'library_books', '/ledger', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('reconciliation', 'hesabaty', 11, 'item', 'NAV.RECONCILIATION', 'fact_check', '/reconciliation', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('reports', 'hesabaty', 12, 'item', 'NAV.REPORTS', 'insert_chart', '/reports', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('settings', 'hesabaty', 13, 'item', 'NAV.SETTINGS', 'tune', '/settings', FALSE, FALSE, 'ADMIN,ACCOUNTANT', 'nav-item', FALSE),
    ('accountants', 'hesabaty', 14, 'item', 'NAV.ACCOUNTANTS_PORTAL', 'groups', '/accountants', FALSE, FALSE, 'ADMIN', 'nav-item', FALSE)
ON CONFLICT (id) DO UPDATE
SET parent_id = EXCLUDED.parent_id,
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
