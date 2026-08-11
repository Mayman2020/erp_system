-- V17 intentionally provides convenient local/demo credentials. Never allow
-- those known credentials to retain business-API access after deployment.
SET search_path TO erp_system, public;

UPDATE users
SET must_change_password = TRUE,
    updated_at = NOW()
WHERE username = 'admin'
  AND password = '$2b$10$49wu0oR2J3vEOrZkEGsLMuLFpKEt3nrQ9pnquwuvfZu2ceMvriOnq';
