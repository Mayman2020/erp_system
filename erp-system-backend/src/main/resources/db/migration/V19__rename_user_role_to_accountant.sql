SET search_path TO erp_system, public;

UPDATE users
SET role = 'ACCOUNTANT'
WHERE role = 'USER';

ALTER TABLE users
    DROP CONSTRAINT IF EXISTS chk_users_role;

ALTER TABLE users
    ADD CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'ACCOUNTANT'));

UPDATE erp_system.ui_menu_items
SET roles_csv = REPLACE(roles_csv, 'USER', 'ACCOUNTANT')
WHERE roles_csv LIKE '%USER%';
