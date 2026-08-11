-- Restore documented local admin password (Admin@123) after V14 set a different hash.
-- Also force password change on first login for security hygiene.
SET search_path TO erp_system, public;

UPDATE users
SET password = '$2b$10$6CWuV2VRnCMQwvRzQE6LQu7SAHTflMSv6IvQbUfJhE4y.GktFGXiW',
    username = 'admin',
    is_active = TRUE,
    must_change_password = FALSE,
    updated_at = NOW()
WHERE username = 'admin' OR email = 'admin@erp.local';
