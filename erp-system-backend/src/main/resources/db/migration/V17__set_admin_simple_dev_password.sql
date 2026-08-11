-- Local/demo administrator credentials requested for development use.
-- Username: admin
-- Password: admin
SET search_path TO erp_system, public;

UPDATE users
SET password = '$2b$10$49wu0oR2J3vEOrZkEGsLMuLFpKEt3nrQ9pnquwuvfZu2ceMvriOnq',
    username = 'admin',
    is_active = TRUE,
    must_change_password = FALSE,
    updated_at = NOW()
WHERE username = 'admin' OR email = 'admin@erp.local';
