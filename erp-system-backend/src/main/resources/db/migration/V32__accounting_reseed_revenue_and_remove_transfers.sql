SET search_path TO erp_system, public;

DELETE FROM role_menu_permissions
WHERE menu_item_id = 'transfers';

DELETE FROM ui_menu_items
WHERE id = 'transfers';

DELETE FROM lookup_values
WHERE type_code = 'transfer-statuses';

DELETE FROM lookup_types
WHERE code = 'transfer-statuses';

UPDATE lookup_values
SET code = 'REVENUE',
    name_en = 'Revenue',
    name_ar = 'الإيرادات',
    sort_order = 4,
    updated_by = 'flyway',
    updated_at = NOW()
WHERE type_code = 'account-types'
  AND code = 'INCOME';

INSERT INTO lookup_values (type_code, code, sort_order, is_active, name_en, name_ar, created_by, updated_by)
SELECT 'account-types', 'REVENUE', 4, TRUE, 'Revenue', 'الإيرادات', 'flyway', 'flyway'
WHERE NOT EXISTS (
    SELECT 1
    FROM lookup_values
    WHERE type_code = 'account-types'
      AND code = 'REVENUE'
);

UPDATE ui_menu_items
SET title_key = 'NAV.JOURNAL_VOUCHERS'
WHERE id = 'journal-entries';

ALTER TABLE erp_system.accounts
DROP CONSTRAINT IF EXISTS chk_accounts_type;

TRUNCATE TABLE
    reconciliation_match_pairs,
    reconciliation_lines,
    reconciliations,
    checks,
    transactions,
    transfers,
    payment_vouchers,
    receipt_vouchers,
    bill_lines,
    bills,
    customer_invoice_lines,
    customer_invoices,
    budgets,
    bank_accounts,
    journal_entry_lines,
    journal_entries,
    accounting_audit_log,
    accounts
RESTART IDENTITY CASCADE;

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
VALUES
    ('1000', 'Assets', 'Assets', 'الأصول', NULL, 'ASSET', 1, 'Assets', TRUE, FALSE, 0, 'DEBIT', 'flyway', 'flyway', 0),
    ('2000', 'Liabilities', 'Liabilities', 'الالتزامات', NULL, 'LIABILITY', 1, 'Liabilities', TRUE, FALSE, 0, 'CREDIT', 'flyway', 'flyway', 0),
    ('3000', 'Equity', 'Equity', 'حقوق الملكية', NULL, 'EQUITY', 1, 'Equity', TRUE, FALSE, 0, 'CREDIT', 'flyway', 'flyway', 0),
    ('4000', 'Revenue', 'Revenue', 'الإيرادات', NULL, 'REVENUE', 1, 'Revenue', TRUE, FALSE, 0, 'CREDIT', 'flyway', 'flyway', 0),
    ('5000', 'Expenses', 'Expenses', 'المصروفات', NULL, 'EXPENSE', 1, 'Expenses', TRUE, FALSE, 0, 'DEBIT', 'flyway', 'flyway', 0);

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '1110', 'Cash on Hand', 'Cash on Hand', 'الصندوق', parent.id, 'ASSET', 2, 'Assets/Cash on Hand', TRUE, TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '1000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '1120', 'Main Bank Account', 'Main Bank Account', 'الحساب البنكي الرئيسي', parent.id, 'ASSET', 2, 'Assets/Main Bank Account', TRUE, TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '1000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '1210', 'Accounts Receivable', 'Accounts Receivable', 'الذمم المدينة', parent.id, 'ASSET', 2, 'Assets/Accounts Receivable', TRUE, TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '1000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '2110', 'Accounts Payable', 'Accounts Payable', 'الذمم الدائنة', parent.id, 'LIABILITY', 2, 'Liabilities/Accounts Payable', TRUE, TRUE, 0, 'CREDIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '2000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '2210', 'Tax Payable', 'Tax Payable', 'ضريبة مستحقة', parent.id, 'LIABILITY', 2, 'Liabilities/Tax Payable', TRUE, TRUE, 0, 'CREDIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '2000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '3110', 'Owner Capital', 'Owner Capital', 'رأس المال', parent.id, 'EQUITY', 2, 'Equity/Owner Capital', TRUE, TRUE, 0, 'CREDIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '3000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '3120', 'Retained Earnings', 'Retained Earnings', 'الأرباح المحتجزة', parent.id, 'EQUITY', 2, 'Equity/Retained Earnings', TRUE, TRUE, 0, 'CREDIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '3000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '4110', 'Sales Revenue', 'Sales Revenue', 'إيرادات المبيعات', parent.id, 'REVENUE', 2, 'Revenue/Sales Revenue', TRUE, TRUE, 0, 'CREDIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '4000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '4120', 'Service Revenue', 'Service Revenue', 'إيرادات الخدمات', parent.id, 'REVENUE', 2, 'Revenue/Service Revenue', TRUE, TRUE, 0, 'CREDIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '4000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5110', 'Operating Expenses', 'Operating Expenses', 'المصروفات التشغيلية', parent.id, 'EXPENSE', 2, 'Expenses/Operating Expenses', TRUE, TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '5000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5120', 'Office Supplies Expense', 'Office Supplies Expense', 'مصروفات مكتبية', parent.id, 'EXPENSE', 2, 'Expenses/Office Supplies Expense', TRUE, TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '5000';

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5130', 'Cost of Goods Sold', 'Cost of Goods Sold', 'تكلفة البضاعة المباعة', parent.id, 'EXPENSE', 2, 'Expenses/Cost of Goods Sold', TRUE, TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '5000';

ALTER TABLE erp_system.accounts
ADD CONSTRAINT chk_accounts_type
CHECK (account_type IN ('ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE'));

INSERT INTO bank_accounts (bank_name, account_number, iban, currency, opening_balance, current_balance, linked_account_id, is_active, created_by, updated_by)
SELECT 'Main Operating Bank', 'ERP-BANK-001', NULL, 'USD', 0, 0, account.id, TRUE, 'flyway', 'flyway'
FROM accounts account
WHERE account.code = '1120';
