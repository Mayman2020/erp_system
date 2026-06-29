-- ERP posting accounts: inventory asset + correct AR/AP links for demo masters.

INSERT INTO accounts (code, name, name_en, name_ar, parent_id, account_type, level, full_path, is_active, opening_balance, opening_balance_side, created_by, updated_by, version)
SELECT '1300', 'Inventory', 'Inventory', 'المخزون', parent.id, 'ASSET', 2, 'Assets/Inventory', TRUE, 0, 'DEBIT', 'flyway', 'flyway', 0
FROM accounts parent
WHERE parent.code = '1000'
  AND NOT EXISTS (SELECT 1 FROM accounts WHERE code = '1300');

UPDATE customers c
SET receivable_account_id = ar.id,
    updated_by = 'flyway',
    updated_at = NOW()
FROM accounts ar
WHERE ar.code = '1210'
  AND c.receivable_account_id IN (SELECT id FROM accounts WHERE code IN ('1120', '1110'));

UPDATE suppliers s
SET payable_account_id = ap.id,
    updated_by = 'flyway',
    updated_at = NOW()
FROM accounts ap
WHERE ap.code = '2110'
  AND s.payable_account_id IN (SELECT id FROM accounts WHERE code IN ('2000', '2100'));
