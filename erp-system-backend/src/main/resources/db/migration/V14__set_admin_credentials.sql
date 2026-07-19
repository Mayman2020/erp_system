SET search_path TO erp_system, public;
UPDATE users SET username = 'admin', password = '$2b$10$49wu0oR2J3vEOrZkEGsLMuLFpKEt3nrQ9pnquwuvfZu2ceMvriOnq', is_active = TRUE, updated_at = NOW()
WHERE username = 'admin' OR email = 'admin@erp.local';
