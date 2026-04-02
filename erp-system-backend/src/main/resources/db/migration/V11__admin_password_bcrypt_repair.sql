SET search_path TO erp_system, public;

-- Repair databases where `admin` password was set to plaintext (Spring expects BCrypt).
-- After this migration: username `admin`, email `admin@erp.local`, password `Admin@123`
UPDATE users
SET password = '$2b$10$6CWuV2VRnCMQwvRzQE6LQu7SAHTflMSv6IvQbUfJhE4y.GktFGXiW',
    updated_at   = NOW()
WHERE username = 'admin'
  AND password NOT LIKE '$2%';
