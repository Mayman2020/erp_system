SET search_path TO erp_system, public;

CREATE TABLE IF NOT EXISTS ui_menu_items (
    id VARCHAR(64) PRIMARY KEY,
    parent_id VARCHAR(64) REFERENCES erp_system.ui_menu_items (id) ON DELETE CASCADE,
    sort_order INT NOT NULL DEFAULT 0,
    item_type VARCHAR(16) NOT NULL,
    title_key VARCHAR(128) NOT NULL,
    icon VARCHAR(64),
    url VARCHAR(512),
    is_external BOOLEAN NOT NULL DEFAULT FALSE,
    target_blank BOOLEAN NOT NULL DEFAULT FALSE,
    roles_csv VARCHAR(256),
    item_classes VARCHAR(128),
    breadcrumbs_flag BOOLEAN
);

CREATE INDEX IF NOT EXISTS idx_ui_menu_items_parent ON erp_system.ui_menu_items (parent_id);
CREATE INDEX IF NOT EXISTS idx_ui_menu_items_sort ON erp_system.ui_menu_items (parent_id, sort_order);

INSERT INTO erp_system.ui_menu_items (id, parent_id, sort_order, item_type, title_key, icon, url, is_external, target_blank, roles_csv, item_classes, breadcrumbs_flag)
VALUES
    ('dashboard', NULL, 0, 'group', 'NAV.DASHBOARD_GROUP', 'menu', NULL, FALSE, FALSE, 'ADMIN,USER', NULL, NULL),
    ('default', 'dashboard', 0, 'item', 'NAV.DASHBOARD_DEFAULT', 'dashboard', '/dashboard/default', FALSE, FALSE, NULL, 'nav-item', FALSE),

    ('accounting', NULL, 1, 'group', 'ACCOUNTING.TITLE', 'account_balance_wallet', NULL, FALSE, FALSE, 'ADMIN', NULL, NULL),
    ('accounting-dashboard', 'accounting', 0, 'item', 'ACCOUNTING.DASHBOARD', 'dashboard', '/accounting/dashboard', FALSE, FALSE, NULL, 'nav-item', FALSE),
    ('chart-of-accounts', 'accounting', 1, 'item', 'ACCOUNTING.CHART_OF_ACCOUNTS', 'account_tree', '/accounting/chart-of-accounts', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('ledger', 'accounting', 2, 'item', 'ACCOUNTING.LEDGER', 'menu_book', '/accounting/ledger', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('journal-entries', 'accounting', 3, 'item', 'ACCOUNTING.JOURNAL_ENTRIES', 'receipt', '/accounting/journal-entries', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('payment-vouchers', 'accounting', 4, 'item', 'ACCOUNTING.PAYMENT_VOUCHERS', 'payment', '/accounting/payment-vouchers', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('receipt-vouchers', 'accounting', 5, 'item', 'ACCOUNTING.RECEIPT_VOUCHERS', 'receipt_long', '/accounting/receipt-vouchers', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('transfers', 'accounting', 6, 'item', 'ACCOUNTING.TRANSFERS', 'swap_horiz', '/accounting/transfers', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('transactions', 'accounting', 7, 'item', 'ACCOUNTING.TRANSACTIONS', 'list_alt', '/accounting/transactions', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('bills', 'accounting', 8, 'item', 'ACCOUNTING.BILLS', 'description', '/accounting/bills', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('checks', 'accounting', 9, 'item', 'ACCOUNTING.CHECKS', 'check', '/accounting/checks', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('bank-accounts', 'accounting', 10, 'item', 'ACCOUNTING.BANK_ACCOUNTS', 'account_balance', '/accounting/bank-accounts', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('registers', 'accounting', 11, 'item', 'ACCOUNTING.REGISTERS', 'table_chart', '/accounting/registers', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('reconciliation', 'accounting', 12, 'item', 'ACCOUNTING.RECONCILIATION', 'compare_arrows', '/accounting/reconciliation', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('budget', 'accounting', 13, 'item', 'ACCOUNTING.BUDGET', 'pie_chart', '/accounting/budget', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('settings', 'accounting', 14, 'item', 'ACCOUNTING.SETTINGS', 'settings', '/accounting/settings', FALSE, FALSE, NULL, 'nav-item', NULL),

    ('authentication', NULL, 2, 'group', 'NAV.AUTH_GROUP', 'lock', NULL, FALSE, FALSE, 'ADMIN,USER', NULL, NULL),
    ('login', 'authentication', 0, 'item', 'NAV.LOGIN', 'login', '/login', FALSE, TRUE, NULL, 'nav-item', FALSE),
    ('register', 'authentication', 1, 'item', 'NAV.REGISTER', 'profile', '/register', FALSE, TRUE, NULL, 'nav-item', FALSE),

    ('utilities', NULL, 3, 'group', 'NAV.UI_COMPONENTS', 'widgets', NULL, FALSE, FALSE, 'ADMIN,USER', NULL, NULL),
    ('typography', 'utilities', 0, 'item', 'NAV.TYPOGRAPHY', 'format_size', '/typography', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('color', 'utilities', 1, 'item', 'NAV.COLORS', 'palette', '/color', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('ant-icons', 'utilities', 2, 'item', 'NAV.ANT_ICONS', 'apps', 'https://ant.design/components/icon', TRUE, TRUE, NULL, 'nav-item', NULL),

    ('other', NULL, 4, 'group', 'NAV.OTHER', 'more_horiz', NULL, FALSE, FALSE, 'ADMIN,USER', NULL, NULL),
    ('sample-page', 'other', 0, 'item', 'NAV.SAMPLE_PAGE', 'article', '/sample-page', FALSE, FALSE, NULL, 'nav-item', NULL),
    ('document', 'other', 1, 'item', 'NAV.DOCUMENT', 'help_outline', 'https://codedthemes.gitbook.io/mantis-angular/', TRUE, TRUE, NULL, 'nav-item', NULL);
