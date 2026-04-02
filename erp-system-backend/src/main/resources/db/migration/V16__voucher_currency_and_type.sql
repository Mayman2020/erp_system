SET search_path TO erp_system, public;

ALTER TABLE payment_vouchers
    ADD COLUMN IF NOT EXISTS currency_code VARCHAR(3),
    ADD COLUMN IF NOT EXISTS voucher_type VARCHAR(30);

ALTER TABLE receipt_vouchers
    ADD COLUMN IF NOT EXISTS currency_code VARCHAR(3),
    ADD COLUMN IF NOT EXISTS voucher_type VARCHAR(30);

UPDATE payment_vouchers
SET currency_code = COALESCE(currency_code, 'USD'),
    voucher_type = COALESCE(voucher_type, 'STANDARD')
WHERE currency_code IS NULL
   OR voucher_type IS NULL;

UPDATE receipt_vouchers
SET currency_code = COALESCE(currency_code, 'USD'),
    voucher_type = COALESCE(voucher_type, 'STANDARD')
WHERE currency_code IS NULL
   OR voucher_type IS NULL;

ALTER TABLE payment_vouchers
    ALTER COLUMN currency_code SET NOT NULL,
    ALTER COLUMN voucher_type SET NOT NULL;

ALTER TABLE receipt_vouchers
    ALTER COLUMN currency_code SET NOT NULL,
    ALTER COLUMN voucher_type SET NOT NULL;
