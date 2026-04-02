-- V10__accounting_reference_seed.sql
-- Seed reference data for dynamic accounting screens

SET search_path TO erp_system, public;

-- Ensure numbering sequences used by UI/API are present.
INSERT INTO numbering_sequences (sequence_name, prefix, current_number, padding_length)
VALUES
('TRANSACTION_REFERENCE', 'TX', 1, 6),
('CUSTOMER_INVOICE', 'INV', 1, 6),
('RECONCILIATION_REFERENCE', 'REC', 1, 6)
ON CONFLICT (sequence_name) DO NOTHING;

-- Seed a sample bank account tied to chart of accounts code 1120 when available.
INSERT INTO bank_accounts (
    bank_name,
    account_number,
    iban,
    currency,
    opening_balance,
    current_balance,
    linked_account_id,
    is_active,
    created_by,
    updated_by
)
SELECT
    'ERP Demo Bank',
    'ERP-0001',
    'SA0000000000000000000001',
    'USD',
    10000,
    10000,
    a.id,
    TRUE,
    'flyway',
    'flyway'
FROM accounts a
WHERE a.code = '1120'
ON CONFLICT (account_number) DO NOTHING;

-- Seed one customer invoice and line for dynamic invoice references.
WITH receivable AS (
    SELECT id FROM accounts WHERE code = '1100' LIMIT 1
),
revenue AS (
    SELECT id FROM accounts WHERE code = '4100' LIMIT 1
),
created_invoice AS (
    INSERT INTO customer_invoices (
        invoice_number,
        invoice_date,
        due_date,
        customer_name,
        customer_reference,
        description,
        subtotal,
        tax_amount,
        total_amount,
        paid_amount,
        outstanding_amount,
        status,
        receivable_account_id,
        revenue_account_id,
        created_by,
        updated_by
    )
    SELECT
        'INV-000001',
        CURRENT_DATE,
        CURRENT_DATE + INTERVAL '30 days',
        'Demo Customer',
        'CUST-001',
        'Seeded invoice for API-driven screens',
        1000,
        150,
        1150,
        0,
        1150,
        'DRAFT',
        receivable.id,
        revenue.id,
        'flyway',
        'flyway'
    FROM receivable, revenue
    ON CONFLICT (invoice_number) DO NOTHING
    RETURNING id
),
invoice_ref AS (
    SELECT id FROM created_invoice
    UNION ALL
    SELECT id FROM customer_invoices WHERE invoice_number = 'INV-000001'
)
INSERT INTO customer_invoice_lines (
    invoice_id,
    account_id,
    description,
    quantity,
    unit_price,
    line_total,
    created_by,
    updated_by
)
SELECT
    invoice_ref.id,
    revenue.id,
    'Seeded invoice line',
    1,
    1000,
    1000,
    'flyway',
    'flyway'
FROM invoice_ref, revenue
WHERE NOT EXISTS (
    SELECT 1
    FROM customer_invoice_lines l
    WHERE l.invoice_id = invoice_ref.id
);
