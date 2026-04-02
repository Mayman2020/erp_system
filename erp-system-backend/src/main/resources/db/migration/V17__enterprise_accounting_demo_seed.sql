SET search_path TO erp_system, public;

-- Chart of accounts (enterprise demo set)
-- Keep compatibility with legacy schema where "name" is still NOT NULL.
INSERT INTO accounts (code, name, name_en, name_ar, account_type, level, full_path, is_active, is_postable, opening_balance, opening_balance_side, created_by, updated_by)
VALUES
('1000', 'Assets', 'Assets', 'الأصول', 'ASSET', 1, 'Assets', TRUE, FALSE, 0, 'DEBIT', 'flyway', 'flyway'),
('1100', 'Cash and Cash Equivalents', 'Cash and Cash Equivalents', 'النقد وما في حكمه', 'ASSET', 2, 'Assets/Cash and Cash Equivalents', TRUE, FALSE, 0, 'DEBIT', 'flyway', 'flyway'),
('1110', 'Cash on Hand', 'Cash on Hand', 'الصندوق', 'ASSET', 3, 'Assets/Cash and Cash Equivalents/Cash on Hand', TRUE, TRUE, 2500.00, 'DEBIT', 'flyway', 'flyway'),
('1120', 'Main Bank Account', 'Main Bank Account', 'الحساب البنكي الرئيسي', 'ASSET', 3, 'Assets/Cash and Cash Equivalents/Main Bank Account', TRUE, TRUE, 35000.00, 'DEBIT', 'flyway', 'flyway'),
('1130', 'Savings Bank Account', 'Savings Bank Account', 'حساب البنك الادخاري', 'ASSET', 3, 'Assets/Cash and Cash Equivalents/Savings Bank Account', TRUE, TRUE, 12000.00, 'DEBIT', 'flyway', 'flyway'),
('1200', 'Accounts Receivable', 'Accounts Receivable', 'الذمم المدينة', 'ASSET', 2, 'Assets/Accounts Receivable', TRUE, TRUE, 18000.00, 'DEBIT', 'flyway', 'flyway'),
('1300', 'Inventory', 'Inventory', 'المخزون', 'ASSET', 2, 'Assets/Inventory', TRUE, TRUE, 22000.00, 'DEBIT', 'flyway', 'flyway'),
('1500', 'Fixed Assets', 'Fixed Assets', 'الأصول الثابتة', 'ASSET', 2, 'Assets/Fixed Assets', TRUE, TRUE, 85000.00, 'DEBIT', 'flyway', 'flyway'),
('2000', 'Liabilities', 'Liabilities', 'الالتزامات', 'LIABILITY', 1, 'Liabilities', TRUE, FALSE, 0, 'CREDIT', 'flyway', 'flyway'),
('2100', 'Accounts Payable', 'Accounts Payable', 'الذمم الدائنة', 'LIABILITY', 2, 'Liabilities/Accounts Payable', TRUE, TRUE, 14000.00, 'CREDIT', 'flyway', 'flyway'),
('2200', 'Accrued Expenses', 'Accrued Expenses', 'المصروفات المستحقة', 'LIABILITY', 2, 'Liabilities/Accrued Expenses', TRUE, TRUE, 6000.00, 'CREDIT', 'flyway', 'flyway'),
('3000', 'Equity', 'Equity', 'حقوق الملكية', 'EQUITY', 1, 'Equity', TRUE, FALSE, 0, 'CREDIT', 'flyway', 'flyway'),
('3100', 'Owner Capital', 'Owner Capital', 'رأس المال', 'EQUITY', 2, 'Equity/Owner Capital', TRUE, TRUE, 90000.00, 'CREDIT', 'flyway', 'flyway'),
('3200', 'Retained Earnings', 'Retained Earnings', 'الأرباح المحتجزة', 'EQUITY', 2, 'Equity/Retained Earnings', TRUE, TRUE, 8000.00, 'CREDIT', 'flyway', 'flyway'),
('4000', 'Revenue', 'Revenue', 'الإيرادات', 'INCOME', 1, 'Revenue', TRUE, FALSE, 0, 'CREDIT', 'flyway', 'flyway'),
('4100', 'Sales Revenue', 'Sales Revenue', 'إيرادات المبيعات', 'INCOME', 2, 'Revenue/Sales Revenue', TRUE, TRUE, 0, 'CREDIT', 'flyway', 'flyway'),
('4200', 'Service Revenue', 'Service Revenue', 'إيرادات الخدمات', 'INCOME', 2, 'Revenue/Service Revenue', TRUE, TRUE, 0, 'CREDIT', 'flyway', 'flyway'),
('5000', 'Expenses', 'Expenses', 'المصروفات', 'EXPENSE', 1, 'Expenses', TRUE, FALSE, 0, 'DEBIT', 'flyway', 'flyway'),
('5100', 'Salary Expense', 'Salary Expense', 'مصروف الرواتب', 'EXPENSE', 2, 'Expenses/Salary Expense', TRUE, TRUE, 0, 'DEBIT', 'flyway', 'flyway'),
('5200', 'Rent Expense', 'Rent Expense', 'مصروف الإيجار', 'EXPENSE', 2, 'Expenses/Rent Expense', TRUE, TRUE, 0, 'DEBIT', 'flyway', 'flyway'),
('5300', 'Utilities Expense', 'Utilities Expense', 'مصروف المرافق', 'EXPENSE', 2, 'Expenses/Utilities Expense', TRUE, TRUE, 0, 'DEBIT', 'flyway', 'flyway'),
('5400', 'Office Supplies Expense', 'Office Supplies Expense', 'مصروف القرطاسية', 'EXPENSE', 2, 'Expenses/Office Supplies Expense', TRUE, TRUE, 0, 'DEBIT', 'flyway', 'flyway')
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    name_en = EXCLUDED.name_en,
    name_ar = EXCLUDED.name_ar,
    account_type = EXCLUDED.account_type,
    is_active = EXCLUDED.is_active,
    is_postable = EXCLUDED.is_postable,
    updated_by = 'flyway';

UPDATE accounts child
SET parent_id = parent.id,
    level = CASE WHEN parent.parent_id IS NULL THEN 2 ELSE 3 END
FROM accounts parent
WHERE child.code IN ('1100', '1200', '1300', '1500', '2100', '2200', '3100', '3200', '4100', '4200', '5100', '5200', '5300', '5400')
  AND (
      (child.code IN ('1100', '1200', '1300', '1500') AND parent.code = '1000')
      OR (child.code IN ('2100', '2200') AND parent.code = '2000')
      OR (child.code IN ('3100', '3200') AND parent.code = '3000')
      OR (child.code IN ('4100', '4200') AND parent.code = '4000')
      OR (child.code IN ('5100', '5200', '5300', '5400') AND parent.code = '5000')
  );

UPDATE accounts child
SET parent_id = parent.id,
    level = 3
FROM accounts parent
WHERE child.code IN ('1110', '1120', '1130')
  AND parent.code = '1100';

-- Bank accounts
UPDATE bank_accounts b
SET bank_name = 'Emirates NBD',
    account_number = 'ENBD-001-USD',
    iban = 'AE070331234567890123456',
    currency = 'USD',
    opening_balance = 35000.00,
    current_balance = 37150.00,
    is_active = TRUE,
    updated_by = 'flyway'
FROM accounts a
WHERE a.code = '1120'
  AND b.linked_account_id = a.id;

INSERT INTO bank_accounts (bank_name, account_number, iban, currency, opening_balance, current_balance, linked_account_id, is_active, created_by, updated_by)
SELECT 'Emirates NBD', 'ENBD-001-USD', 'AE070331234567890123456', 'USD', 35000.00, 37150.00, a.id, TRUE, 'flyway', 'flyway'
FROM accounts a
WHERE a.code = '1120'
  AND NOT EXISTS (
      SELECT 1
      FROM bank_accounts b
      WHERE b.linked_account_id = a.id
         OR b.account_number = 'ENBD-001-USD'
  );

UPDATE bank_accounts b
SET bank_name = 'Abu Dhabi Islamic Bank',
    account_number = 'ADIB-002-AED',
    iban = 'AE120331234567890123457',
    currency = 'AED',
    opening_balance = 12000.00,
    current_balance = 14520.00,
    is_active = TRUE,
    updated_by = 'flyway'
FROM accounts a
WHERE a.code = '1130'
  AND b.linked_account_id = a.id;

INSERT INTO bank_accounts (bank_name, account_number, iban, currency, opening_balance, current_balance, linked_account_id, is_active, created_by, updated_by)
SELECT 'Abu Dhabi Islamic Bank', 'ADIB-002-AED', 'AE120331234567890123457', 'AED', 12000.00, 14520.00, a.id, TRUE, 'flyway', 'flyway'
FROM accounts a
WHERE a.code = '1130'
  AND NOT EXISTS (
      SELECT 1
      FROM bank_accounts b
      WHERE b.linked_account_id = a.id
         OR b.account_number = 'ADIB-002-AED'
  );

-- Posted journals for meaningful P&L and balance sheet
WITH header AS (
    INSERT INTO journal_entries (
        entry_date, reference_number, description, status, total_debit, total_credit, posted_at, posted_by,
        source_module, source_record_id, external_reference, currency_code, entry_type, created_by, updated_by
    )
    VALUES
    (CURRENT_DATE - 25, 'JE-DEMO-0001', 'Demo sale invoice posting', 'POSTED', 8500.00, 8500.00, NOW(), 'seed', 'DEMO', 1, 'INV-DEMO-001', 'USD', 'MANUAL', 'flyway', 'flyway'),
    (CURRENT_DATE - 18, 'JE-DEMO-0002', 'Demo salary payment', 'POSTED', 3200.00, 3200.00, NOW(), 'seed', 'DEMO', 2, 'PAY-DEMO-001', 'USD', 'MANUAL', 'flyway', 'flyway'),
    (CURRENT_DATE - 10, 'JE-DEMO-0003', 'Demo rent and utilities', 'POSTED', 2100.00, 2100.00, NOW(), 'seed', 'DEMO', 3, 'PAY-DEMO-002', 'USD', 'MANUAL', 'flyway', 'flyway'),
    (CURRENT_DATE - 5, 'JE-DEMO-0004', 'Demo service receipt', 'POSTED', 4400.00, 4400.00, NOW(), 'seed', 'DEMO', 4, 'REC-DEMO-001', 'USD', 'MANUAL', 'flyway', 'flyway')
    ON CONFLICT (reference_number) DO NOTHING
    RETURNING id, reference_number
)
SELECT 1;

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 1, ar.id, 8500.00, 0.00, 'Accounts receivable debit', 'flyway', 'flyway'
FROM journal_entries je
JOIN accounts ar ON ar.code = '1200'
WHERE je.reference_number = 'JE-DEMO-0001'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 1);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 2, rev.id, 0.00, 8500.00, 'Sales revenue credit', 'flyway', 'flyway'
FROM journal_entries je
JOIN accounts rev ON rev.code = '4100'
WHERE je.reference_number = 'JE-DEMO-0001'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 2);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 1, sal.id, 3200.00, 0.00, 'Salary expense debit', 'flyway', 'flyway'
FROM journal_entries je
JOIN accounts sal ON sal.code = '5100'
WHERE je.reference_number = 'JE-DEMO-0002'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 1);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 2, bank.id, 0.00, 3200.00, 'Bank credit', 'flyway', 'flyway'
FROM journal_entries je
JOIN accounts bank ON bank.code = '1120'
WHERE je.reference_number = 'JE-DEMO-0002'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 2);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 1, rent.id, 1700.00, 0.00, 'Rent expense debit', 'flyway', 'flyway'
FROM journal_entries je
JOIN accounts rent ON rent.code = '5200'
WHERE je.reference_number = 'JE-DEMO-0003'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 1);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 2, util.id, 400.00, 0.00, 'Utilities expense debit', 'flyway', 'flyway'
FROM journal_entries je
JOIN accounts util ON util.code = '5300'
WHERE je.reference_number = 'JE-DEMO-0003'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 2);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 3, bank.id, 0.00, 2100.00, 'Bank credit', 'flyway', 'flyway'
FROM journal_entries je
JOIN accounts bank ON bank.code = '1120'
WHERE je.reference_number = 'JE-DEMO-0003'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 3);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 1, bank.id, 4400.00, 0.00, 'Bank debit', 'flyway', 'flyway'
FROM journal_entries je
JOIN accounts bank ON bank.code = '1120'
WHERE je.reference_number = 'JE-DEMO-0004'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 1);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 2, srv.id, 0.00, 4400.00, 'Service revenue credit', 'flyway', 'flyway'
FROM journal_entries je
JOIN accounts srv ON srv.code = '4200'
WHERE je.reference_number = 'JE-DEMO-0004'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 2);

-- Payment vouchers (one posted, one approved, one draft)
INSERT INTO payment_vouchers (
    voucher_date, reference, description, amount, status, payment_method, party_name, linked_document_reference,
    currency_code, voucher_type, account_id, cash_account_id, expense_account_id, journal_entry_id,
    approved_at, approved_by, posted_at, posted_by, created_by, updated_by
)
SELECT CURRENT_DATE - 18, 'PV-DEMO-0001', 'Salary payment for March payroll', 3200.00, 'POSTED', 'BANK', 'Team Payroll', 'PAYROLL-MAR',
       'USD', 'STANDARD', expense.id, cash.id, expense.id, je.id, NOW() - INTERVAL '19 days', 'seed', NOW() - INTERVAL '18 days', 'seed', 'flyway', 'flyway'
FROM accounts cash
JOIN accounts expense ON expense.code = '5100'
JOIN journal_entries je ON je.reference_number = 'JE-DEMO-0002'
WHERE cash.code = '1120'
  AND NOT EXISTS (SELECT 1 FROM payment_vouchers pv WHERE pv.reference = 'PV-DEMO-0001');

INSERT INTO payment_vouchers (
    voucher_date, reference, description, amount, status, payment_method, party_name, linked_document_reference,
    currency_code, voucher_type, account_id, cash_account_id, expense_account_id, journal_entry_id,
    approved_at, approved_by, created_by, updated_by
)
SELECT CURRENT_DATE - 2, 'PV-DEMO-0002', 'Office supplies pending payment', 650.00, 'APPROVED', 'BANK', 'Office Mart LLC', 'BILL-2026-044',
       'USD', 'BILL_PAYMENT', expense.id, cash.id, expense.id, je.id, NOW() - INTERVAL '1 day', 'seed', 'flyway', 'flyway'
FROM accounts cash
JOIN accounts expense ON expense.code = '5400'
JOIN journal_entries je ON je.reference_number = 'JE-DEMO-0003'
WHERE cash.code = '1120'
  AND NOT EXISTS (SELECT 1 FROM payment_vouchers pv WHERE pv.reference = 'PV-DEMO-0002');

INSERT INTO payment_vouchers (
    voucher_date, reference, description, amount, status, payment_method, party_name, linked_document_reference,
    currency_code, voucher_type, account_id, cash_account_id, expense_account_id, journal_entry_id, created_by, updated_by
)
SELECT CURRENT_DATE, 'PV-DEMO-0003', 'Accrued expense draft settlement', 900.00, 'DRAFT', 'CASH', 'Facilities Vendor', 'ACC-EXP-11',
       'USD', 'STANDARD', expense.id, cash.id, expense.id, je.id, 'flyway', 'flyway'
FROM accounts cash
JOIN accounts expense ON expense.code = '2200'
JOIN journal_entries je ON je.reference_number = 'JE-DEMO-0001'
WHERE cash.code = '1110'
  AND NOT EXISTS (SELECT 1 FROM payment_vouchers pv WHERE pv.reference = 'PV-DEMO-0003');

-- Receipt vouchers (one posted, one approved, one draft)
INSERT INTO receipt_vouchers (
    voucher_date, reference, description, amount, status, payment_method, party_name, invoice_reference,
    currency_code, voucher_type, account_id, cash_account_id, revenue_account_id, journal_entry_id,
    approved_at, approved_by, posted_at, posted_by, created_by, updated_by
)
SELECT CURRENT_DATE - 5, 'RV-DEMO-0001', 'Service revenue receipt', 4400.00, 'POSTED', 'BANK', 'Acme Services Client', 'INV-DEMO-020',
       'USD', 'INVOICE_COLLECTION', revenue.id, cash.id, revenue.id, je.id, NOW() - INTERVAL '6 days', 'seed', NOW() - INTERVAL '5 days', 'seed', 'flyway', 'flyway'
FROM accounts cash
JOIN accounts revenue ON revenue.code = '4200'
JOIN journal_entries je ON je.reference_number = 'JE-DEMO-0004'
WHERE cash.code = '1120'
  AND NOT EXISTS (SELECT 1 FROM receipt_vouchers rv WHERE rv.reference = 'RV-DEMO-0001');

INSERT INTO receipt_vouchers (
    voucher_date, reference, description, amount, status, payment_method, party_name, invoice_reference,
    currency_code, voucher_type, account_id, cash_account_id, revenue_account_id, journal_entry_id, approved_at, approved_by, created_by, updated_by
)
SELECT CURRENT_DATE - 1, 'RV-DEMO-0002', 'Advance customer collection', 1850.00, 'APPROVED', 'BANK', 'Blue Ocean Trading', 'ADV-CUST-04',
       'USD', 'ADVANCE', revenue.id, cash.id, revenue.id, je.id, NOW() - INTERVAL '1 day', 'seed', 'flyway', 'flyway'
FROM accounts cash
JOIN accounts revenue ON revenue.code = '4100'
JOIN journal_entries je ON je.reference_number = 'JE-DEMO-0001'
WHERE cash.code = '1120'
  AND NOT EXISTS (SELECT 1 FROM receipt_vouchers rv WHERE rv.reference = 'RV-DEMO-0002');

INSERT INTO receipt_vouchers (
    voucher_date, reference, description, amount, status, payment_method, party_name, invoice_reference,
    currency_code, voucher_type, account_id, cash_account_id, revenue_account_id, journal_entry_id, created_by, updated_by
)
SELECT CURRENT_DATE, 'RV-DEMO-0003', 'Cash collection draft', 700.00, 'DRAFT', 'CASH', 'Walk-in Customer', 'POS-044',
       'USD', 'STANDARD', revenue.id, cash.id, revenue.id, je.id, 'flyway', 'flyway'
FROM accounts cash
JOIN accounts revenue ON revenue.code = '4100'
JOIN journal_entries je ON je.reference_number = 'JE-DEMO-0003'
WHERE cash.code = '1110'
  AND NOT EXISTS (SELECT 1 FROM receipt_vouchers rv WHERE rv.reference = 'RV-DEMO-0003');

-- Transactions history
INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount, debit_account_id, credit_account_id,
    related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 5, 'TX-DEMO-0001', 'Bank receipt transaction', 'SALE', 'COMPLETED', 4400.00, debit_acc.id, credit_acc.id, 'RV-DEMO-0001', 'flyway', 'flyway'
FROM accounts debit_acc
JOIN accounts credit_acc ON credit_acc.code = '4200'
WHERE debit_acc.code = '1120'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0001');

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount, debit_account_id, credit_account_id,
    related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 18, 'TX-DEMO-0002', 'Salary payment transaction', 'PURCHASE', 'COMPLETED', 3200.00, debit_acc.id, credit_acc.id, 'PV-DEMO-0001', 'flyway', 'flyway'
FROM accounts debit_acc
JOIN accounts credit_acc ON credit_acc.code = '1120'
WHERE debit_acc.code = '5100'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0002');

-- Reconciliation sample with matched + unmatched lines
INSERT INTO reconciliations (
    bank_account_id, statement_start_date, statement_end_date, opening_balance, closing_balance,
    system_ending_balance, difference, status, created_by, updated_by
)
SELECT b.id, CURRENT_DATE - 30, CURRENT_DATE, 35000.00, 37150.00, 37150.00, 0.00, 'OPEN', 'flyway', 'flyway'
FROM bank_accounts b
WHERE b.account_number = 'ENBD-001-USD'
  AND NOT EXISTS (
      SELECT 1 FROM reconciliations r
      WHERE r.bank_account_id = b.id
        AND r.statement_start_date = CURRENT_DATE - 30
        AND r.statement_end_date = CURRENT_DATE
  );

WITH rec AS (
    SELECT id
    FROM reconciliations
    WHERE statement_start_date = CURRENT_DATE - 30
      AND statement_end_date = CURRENT_DATE
    ORDER BY id DESC
    LIMIT 1
)
INSERT INTO reconciliation_lines (
    reconciliation_id, transaction_date, description, amount, transaction_type, status, source_reference,
    journal_entry_line_id, matched_line_id, matched_amount, created_by, updated_by
)
SELECT rec.id, CURRENT_DATE - 5, 'Bank statement service receipt', 4400.00, 'BANK_STATEMENT', 'MATCHED', 'STMT-001',
       NULL, NULL, 4400.00, 'flyway', 'flyway'
FROM rec
WHERE NOT EXISTS (
    SELECT 1 FROM reconciliation_lines line
    WHERE line.reconciliation_id = rec.id
      AND line.source_reference = 'STMT-001'
);

WITH rec AS (
    SELECT id
    FROM reconciliations
    WHERE statement_start_date = CURRENT_DATE - 30
      AND statement_end_date = CURRENT_DATE
    ORDER BY id DESC
    LIMIT 1
)
INSERT INTO reconciliation_lines (
    reconciliation_id, transaction_date, description, amount, transaction_type, status, source_reference,
    journal_entry_line_id, matched_line_id, matched_amount, created_by, updated_by
)
SELECT rec.id, CURRENT_DATE - 4, 'Monthly bank fee', 75.00, 'BANK_STATEMENT', 'UNMATCHED', 'STMT-002',
       NULL, NULL, NULL, 'flyway', 'flyway'
FROM rec
WHERE NOT EXISTS (
    SELECT 1 FROM reconciliation_lines line
    WHERE line.reconciliation_id = rec.id
      AND line.source_reference = 'STMT-002'
);

WITH rec AS (
    SELECT id
    FROM reconciliations
    WHERE statement_start_date = CURRENT_DATE - 30
      AND statement_end_date = CURRENT_DATE
    ORDER BY id DESC
    LIMIT 1
),
je_line AS (
    SELECT line.id
    FROM journal_entry_lines line
    JOIN journal_entries je ON je.id = line.journal_entry_id
    JOIN accounts acc ON acc.id = line.account_id
    WHERE je.reference_number = 'JE-DEMO-0004'
      AND acc.code = '1120'
    LIMIT 1
)
INSERT INTO reconciliation_lines (
    reconciliation_id, transaction_date, description, amount, transaction_type, status, source_reference,
    journal_entry_line_id, matched_line_id, matched_amount, created_by, updated_by
)
SELECT rec.id, CURRENT_DATE - 5, 'ERP receipt posting', 4400.00, 'SYSTEM_TRANSACTION', 'MATCHED', 'JE-DEMO-0004',
       je_line.id, NULL, 4400.00, 'flyway', 'flyway'
FROM rec, je_line
WHERE NOT EXISTS (
    SELECT 1 FROM reconciliation_lines line
    WHERE line.reconciliation_id = rec.id
      AND line.source_reference = 'JE-DEMO-0004'
);

WITH rec AS (
    SELECT id
    FROM reconciliations
    WHERE statement_start_date = CURRENT_DATE - 30
      AND statement_end_date = CURRENT_DATE
    ORDER BY id DESC
    LIMIT 1
),
je_line AS (
    SELECT line.id
    FROM journal_entry_lines line
    JOIN journal_entries je ON je.id = line.journal_entry_id
    JOIN accounts acc ON acc.id = line.account_id
    WHERE je.reference_number = 'JE-DEMO-0002'
      AND acc.code = '1120'
    LIMIT 1
)
INSERT INTO reconciliation_lines (
    reconciliation_id, transaction_date, description, amount, transaction_type, status, source_reference,
    journal_entry_line_id, matched_line_id, matched_amount, created_by, updated_by
)
SELECT rec.id, CURRENT_DATE - 18, 'ERP salary disbursement', 3200.00, 'SYSTEM_TRANSACTION', 'UNMATCHED', 'JE-DEMO-0002',
       je_line.id, NULL, NULL, 'flyway', 'flyway'
FROM rec, je_line
WHERE NOT EXISTS (
    SELECT 1 FROM reconciliation_lines line
    WHERE line.reconciliation_id = rec.id
      AND line.source_reference = 'JE-DEMO-0002'
);
