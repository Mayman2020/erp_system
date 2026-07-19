SET search_path TO erp_system;

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    actor_user_id BIGINT NULL REFERENCES users(id) ON DELETE SET NULL,
    type VARCHAR(80) NOT NULL,
    title_key VARCHAR(200) NOT NULL,
    body_key VARCHAR(200) NOT NULL,
    vars_json TEXT NULL,
    reference_type VARCHAR(80) NULL,
    reference_id BIGINT NULL,
    read_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_notifications_recipient_created
    ON notifications (recipient_user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_notifications_recipient_unread
    ON notifications (recipient_user_id)
    WHERE read_at IS NULL;
