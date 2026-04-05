SET search_path TO erp_system, public;

-- ============================================================
-- V40: Fix demo data that failed in V39 due to wrong account codes
-- Correct codes: 1210 (AR), 4110 (Sales), 4120 (Service),
--   2110 (AP), 2210 (Tax Payable), 5110 (OpEx), 5120 (Supplies), 5130 (COGS)
-- ============================================================

-- ==================== Customer Invoices ====================

INSERT INTO customer_invoices (
    invoice_number, invoice_date, due_date, customer_name, customer_reference,
    description, subtotal, tax_amount, total_amount, paid_amount, outstanding_amount,
    status, receivable_account_id, revenue_account_id, created_by, updated_by
)
SELECT 'INV-2026-001', CURRENT_DATE - 45, CURRENT_DATE - 15,
       'Global Trading LLC', 'GT-REF-100',
       'Consulting services for Q1 2026', 12000.00, 600.00, 12600.00, 12600.00, 0.00,
       'PAID', ar.id, rev.id, 'flyway', 'flyway'
FROM accounts ar JOIN accounts rev ON rev.code = '4110' WHERE ar.code = '1210'
  AND NOT EXISTS (SELECT 1 FROM customer_invoices ci WHERE ci.invoice_number = 'INV-2026-001');

INSERT INTO customer_invoices (
    invoice_number, invoice_date, due_date, customer_name, customer_reference,
    description, subtotal, tax_amount, total_amount, paid_amount, outstanding_amount,
    status, receivable_account_id, revenue_account_id, created_by, updated_by
)
SELECT 'INV-2026-002', CURRENT_DATE - 30, CURRENT_DATE - 1,
       'Blue Ocean Technologies', 'BOT-2026-Q1',
       'Software development - Phase 1', 25000.00, 1250.00, 26250.00, 15000.00, 11250.00,
       'PARTIAL', ar.id, rev.id, 'flyway', 'flyway'
FROM accounts ar JOIN accounts rev ON rev.code = '4120' WHERE ar.code = '1210'
  AND NOT EXISTS (SELECT 1 FROM customer_invoices ci WHERE ci.invoice_number = 'INV-2026-002');

INSERT INTO customer_invoices (
    invoice_number, invoice_date, due_date, customer_name, customer_reference,
    description, subtotal, tax_amount, total_amount, paid_amount, outstanding_amount,
    status, receivable_account_id, revenue_account_id,
    posted_at, posted_by, created_by, updated_by
)
SELECT 'INV-2026-003', CURRENT_DATE - 15, CURRENT_DATE + 15,
       'Desert Star Enterprises', 'DSE-ADV-07',
       'IT infrastructure setup', 8500.00, 425.00, 8925.00, 0.00, 8925.00,
       'POSTED', ar.id, rev.id, NOW() - INTERVAL '14 days', 'admin', 'flyway', 'flyway'
FROM accounts ar JOIN accounts rev ON rev.code = '4120' WHERE ar.code = '1210'
  AND NOT EXISTS (SELECT 1 FROM customer_invoices ci WHERE ci.invoice_number = 'INV-2026-003');

INSERT INTO customer_invoices (
    invoice_number, invoice_date, due_date, customer_name, customer_reference,
    description, subtotal, tax_amount, total_amount, paid_amount, outstanding_amount,
    status, receivable_account_id, revenue_account_id, created_by, updated_by
)
SELECT 'INV-2026-004', CURRENT_DATE - 5, CURRENT_DATE + 25,
       'Al Manara Group', 'AMG-2026-011',
       'Monthly maintenance contract', 3500.00, 175.00, 3675.00, 0.00, 3675.00,
       'DRAFT', ar.id, rev.id, 'flyway', 'flyway'
FROM accounts ar JOIN accounts rev ON rev.code = '4110' WHERE ar.code = '1210'
  AND NOT EXISTS (SELECT 1 FROM customer_invoices ci WHERE ci.invoice_number = 'INV-2026-004');

INSERT INTO customer_invoices (
    invoice_number, invoice_date, due_date, customer_name, customer_reference,
    description, subtotal, tax_amount, total_amount, paid_amount, outstanding_amount,
    status, receivable_account_id, revenue_account_id,
    cancelled_at, cancelled_by, created_by, updated_by
)
SELECT 'INV-2026-005', CURRENT_DATE - 60, CURRENT_DATE - 30,
       'Phoenix Industries', 'PHX-OLD-009',
       'Cancelled order - equipment supply', 6200.00, 310.00, 6510.00, 0.00, 0.00,
       'CANCELLED', ar.id, rev.id, NOW() - INTERVAL '28 days', 'admin', 'flyway', 'flyway'
FROM accounts ar JOIN accounts rev ON rev.code = '4110' WHERE ar.code = '1210'
  AND NOT EXISTS (SELECT 1 FROM customer_invoices ci WHERE ci.invoice_number = 'INV-2026-005');

-- ==================== Transactions ====================

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 45, 'TX-DEMO-0003', 'Invoice payment received from Global Trading',
       'SALE', 'POSTED', 12600.00, debit_acc.id, credit_acc.id, 'INV-2026-001', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '4110'
WHERE debit_acc.code = '1120'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0003');

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 30, 'TX-DEMO-0004', 'Partial payment from Blue Ocean Technologies',
       'SALE', 'POSTED', 15000.00, debit_acc.id, credit_acc.id, 'INV-2026-002', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '4120'
WHERE debit_acc.code = '1120'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0004');

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 10, 'TX-DEMO-0005', 'Rent payment for office lease',
       'PURCHASE', 'POSTED', 1700.00, debit_acc.id, credit_acc.id, 'PV-DEMO-002', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '1120'
WHERE debit_acc.code = '5110'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0005');

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 3, 'TX-DEMO-0006', 'Utilities payment - electricity and water',
       'PURCHASE', 'POSTED', 400.00, debit_acc.id, credit_acc.id, 'UTIL-MAR', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '1110'
WHERE debit_acc.code = '5120'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0006');

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 1, 'TX-DEMO-0007', 'Inventory restocking purchase',
       'PURCHASE', 'DRAFT', 4500.00, debit_acc.id, credit_acc.id, 'PO-2026-015', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '2110'
WHERE debit_acc.code = '5130'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0007');

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE, 'TX-DEMO-0008', 'Office supplies purchase',
       'PURCHASE', 'DRAFT', 650.00, debit_acc.id, credit_acc.id, 'BILL-2026-044', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '1120'
WHERE debit_acc.code = '5120'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0008');

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 20, 'TX-DEMO-0009', 'Client consulting fee - Desert Star',
       'SALE', 'POSTED', 8925.00, debit_acc.id, credit_acc.id, 'INV-2026-003', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '4120'
WHERE debit_acc.code = '1210'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0009');

INSERT INTO transactions (
    transaction_date, reference, description, transaction_type, status, amount,
    debit_account_id, credit_account_id, related_document_reference, created_by, updated_by
)
SELECT CURRENT_DATE - 7, 'TX-DEMO-0010', 'Employee salary payment - March 2026',
       'PURCHASE', 'POSTED', 9500.00, debit_acc.id, credit_acc.id, 'SAL-MAR-2026', 'flyway', 'flyway'
FROM accounts debit_acc JOIN accounts credit_acc ON credit_acc.code = '1120'
WHERE debit_acc.code = '5110'
  AND NOT EXISTS (SELECT 1 FROM transactions t WHERE t.reference = 'TX-DEMO-0010');

-- ==================== Journal Entries ====================

INSERT INTO journal_entries (
    entry_date, reference_number, description, status, total_debit, total_credit,
    source_module, currency_code, entry_type, created_by, updated_by
)
SELECT CURRENT_DATE - 2, 'JE-DEMO-0005', 'Pending inventory adjustment', 'DRAFT', 1500.00, 1500.00,
       'MANUAL', 'USD', 'MANUAL', 'flyway', 'flyway'
WHERE NOT EXISTS (SELECT 1 FROM journal_entries je WHERE je.reference_number = 'JE-DEMO-0005');

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 1, acc.id, 1500.00, 0.00, 'Inventory increase', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '1210'
WHERE je.reference_number = 'JE-DEMO-0005'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 1);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 2, acc.id, 0.00, 1500.00, 'Accounts payable increase', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '2110'
WHERE je.reference_number = 'JE-DEMO-0005'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 2);

INSERT INTO journal_entries (
    entry_date, reference_number, description, status, total_debit, total_credit,
    posted_at, posted_by, source_module, currency_code, entry_type, created_by, updated_by
)
SELECT CURRENT_DATE - 8, 'JE-DEMO-0006', 'Approved accrual adjustment', 'APPROVED', 2000.00, 2000.00,
       NOW() - INTERVAL '7 days', 'admin', 'MANUAL', 'USD', 'MANUAL', 'flyway', 'flyway'
WHERE NOT EXISTS (SELECT 1 FROM journal_entries je WHERE je.reference_number = 'JE-DEMO-0006');

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 1, acc.id, 2000.00, 0.00, 'Accrued expense recognition', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '5110'
WHERE je.reference_number = 'JE-DEMO-0006'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 1);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 2, acc.id, 0.00, 2000.00, 'Accrued liabilities', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '2210'
WHERE je.reference_number = 'JE-DEMO-0006'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 2);

INSERT INTO journal_entries (
    entry_date, reference_number, description, status, total_debit, total_credit,
    posted_at, posted_by, source_module, currency_code, entry_type, created_by, updated_by
)
SELECT CURRENT_DATE - 25, 'JE-DEMO-0007', 'Revenue recognition - consulting', 'APPROVED', 8500.00, 8500.00,
       NOW() - INTERVAL '24 days', 'admin', 'MANUAL', 'USD', 'MANUAL', 'flyway', 'flyway'
WHERE NOT EXISTS (SELECT 1 FROM journal_entries je WHERE je.reference_number = 'JE-DEMO-0007');

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 1, acc.id, 8500.00, 0.00, 'Accounts receivable - Desert Star', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '1210'
WHERE je.reference_number = 'JE-DEMO-0007'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 1);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 2, acc.id, 0.00, 8500.00, 'Service revenue - consulting', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '4120'
WHERE je.reference_number = 'JE-DEMO-0007'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 2);

INSERT INTO journal_entries (
    entry_date, reference_number, description, status, total_debit, total_credit,
    source_module, currency_code, entry_type, created_by, updated_by
)
SELECT CURRENT_DATE - 1, 'JE-DEMO-0008', 'Salary accrual - March 2026', 'DRAFT', 9500.00, 9500.00,
       'MANUAL', 'USD', 'MANUAL', 'flyway', 'flyway'
WHERE NOT EXISTS (SELECT 1 FROM journal_entries je WHERE je.reference_number = 'JE-DEMO-0008');

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 1, acc.id, 9500.00, 0.00, 'Salary expense', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '5110'
WHERE je.reference_number = 'JE-DEMO-0008'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 1);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 2, acc.id, 0.00, 9500.00, 'Salary payable', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '2110'
WHERE je.reference_number = 'JE-DEMO-0008'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 2);

INSERT INTO journal_entries (
    entry_date, reference_number, description, status, total_debit, total_credit,
    posted_at, posted_by, source_module, currency_code, entry_type, created_by, updated_by
)
SELECT CURRENT_DATE - 40, 'JE-DEMO-0009', 'Opening balance adjustment', 'APPROVED', 50000.00, 50000.00,
       NOW() - INTERVAL '39 days', 'admin', 'MANUAL', 'USD', 'MANUAL', 'flyway', 'flyway'
WHERE NOT EXISTS (SELECT 1 FROM journal_entries je WHERE je.reference_number = 'JE-DEMO-0009');

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 1, acc.id, 50000.00, 0.00, 'Cash opening balance', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '1120'
WHERE je.reference_number = 'JE-DEMO-0009'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 1);

INSERT INTO journal_entry_lines (journal_entry_id, line_number, account_id, debit, credit, description, created_by, updated_by)
SELECT je.id, 2, acc.id, 0.00, 50000.00, 'Owner capital contribution', 'flyway', 'flyway'
FROM journal_entries je JOIN accounts acc ON acc.code = '3110'
WHERE je.reference_number = 'JE-DEMO-0009'
  AND NOT EXISTS (SELECT 1 FROM journal_entry_lines l WHERE l.journal_entry_id = je.id AND l.line_number = 2);

-- ==================== Reconciliation Demo Data ====================

INSERT INTO reconciliations (
    bank_account_id, statement_start_date, statement_end_date,
    opening_balance, closing_balance, system_ending_balance, difference,
    status, created_by, updated_by
)
SELECT ba.id, CURRENT_DATE - 35, CURRENT_DATE - 5,
       45000.00, 48500.00, 48500.00, 0.00,
       'OPEN', 'flyway', 'flyway'
FROM bank_accounts ba
WHERE ba.is_active = true
  AND NOT EXISTS (SELECT 1 FROM reconciliations r WHERE r.bank_account_id = ba.id AND r.statement_end_date = CURRENT_DATE - 5)
LIMIT 1;
