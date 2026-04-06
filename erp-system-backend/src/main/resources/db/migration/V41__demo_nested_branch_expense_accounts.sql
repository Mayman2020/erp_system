SET search_path TO erp_system, public;

-- ============================================================
-- V41: Nested "branch" expense accounts for UI / hierarchy tests
-- Parent accounts are non-posting groups; only leaf accounts are intended for posting.
-- Structure: Expenses → Branch operations → regions → branches → sub-branches (leaf)
-- ============================================================

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5500', 'Branch operations', 'Branch operations', 'عمليات الفروع', p.id, 'EXPENSE', 2, 'Expenses/Branch operations', TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '5000'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '5500');

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5510', 'Northern branches', 'Northern branches', 'فروع الشمال', p.id, 'EXPENSE', 3, 'Expenses/Branch operations/Northern branches', TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '5500'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '5510');

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5520', 'Greater Cairo region', 'Greater Cairo region', 'منطقة القاهرة الكبرى', p.id, 'EXPENSE', 3, 'Expenses/Branch operations/Greater Cairo region', TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '5500'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '5520');

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5511', 'Alexandria branch', 'Alexandria branch', 'فرع الإسكندرية', p.id, 'EXPENSE', 4, 'Expenses/Branch operations/Northern branches/Alexandria branch', TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '5510'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '5511');

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5512', 'North Coast kiosk', 'North Coast kiosk', 'كشك الساحل الشمالي', p.id, 'EXPENSE', 4, 'Expenses/Branch operations/Northern branches/North Coast kiosk', TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '5510'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '5512');

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5521', 'Cairo HQ', 'Cairo HQ', 'مقر القاهرة', p.id, 'EXPENSE', 4, 'Expenses/Branch operations/Greater Cairo region/Cairo HQ', TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '5520'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '5521');

-- Branch under a branch (Giza nested under Greater Cairo)
INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5530', 'Giza branch', 'Giza branch', 'فرع الجيزة', p.id, 'EXPENSE', 4, 'Expenses/Branch operations/Greater Cairo region/Giza branch', TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '5520'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '5530');

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5531', 'Giza warehouse', 'Giza warehouse', 'مخزن الجيزة', p.id, 'EXPENSE', 5, 'Expenses/Branch operations/Greater Cairo region/Giza branch/Giza warehouse', TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '5530'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '5531');

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '5532', 'Giza retail', 'Giza retail', 'تجزئة الجيزة', p.id, 'EXPENSE', 5, 'Expenses/Branch operations/Greater Cairo region/Giza branch/Giza retail', TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '5530'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '5532');
