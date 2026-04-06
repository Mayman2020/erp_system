SET search_path TO erp_system, public;

-- ============================================================
-- V42: Asset hierarchy — Banks (بنوك) under Assets, Bank Muscat (بنك مسقط) leaf + demo bank row
-- ============================================================

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '1140', 'Banks', 'Banks', 'بنوك', p.id, 'ASSET', 2, 'Assets/Banks', TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '1000'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '1140');

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '1141', 'Bank Muscat', 'Bank Muscat', 'بنك مسقط', p.id, 'ASSET', 3, 'Assets/Banks/Bank Muscat', TRUE, 8750.50, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts p
WHERE p.code = '1140'
  AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.code = '1141');

INSERT INTO bank_accounts (bank_name, account_number, iban, currency, opening_balance, current_balance, linked_account_id, is_active, created_by, updated_by)
SELECT 'Bank Muscat', 'MCT-OM-001234567', 'OM120000000000987654321', 'OMR', 8750.50, 8750.50, acc.id, TRUE, 'flyway', 'flyway'
FROM accounts acc
WHERE acc.code = '1141'
  AND NOT EXISTS (SELECT 1 FROM bank_accounts b WHERE b.linked_account_id = acc.id);
