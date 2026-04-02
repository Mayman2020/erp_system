SET search_path TO erp_system, public;

CREATE TABLE IF NOT EXISTS reconciliation_match_pairs (
    id                  BIGSERIAL PRIMARY KEY,
    reconciliation_id   BIGINT NOT NULL REFERENCES reconciliations(id),
    statement_line_id   BIGINT NOT NULL REFERENCES reconciliation_lines(id),
    system_line_id      BIGINT NOT NULL REFERENCES reconciliation_lines(id),
    matched_amount      NUMERIC(19, 2) NOT NULL,
    matched_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    matched_by          VARCHAR(100) NOT NULL,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    unmatched_at        TIMESTAMPTZ,
    unmatched_by        VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_match_pairs_recon ON reconciliation_match_pairs (reconciliation_id);
CREATE INDEX IF NOT EXISTS idx_match_pairs_stmt ON reconciliation_match_pairs (statement_line_id);
CREATE INDEX IF NOT EXISTS idx_match_pairs_sys ON reconciliation_match_pairs (system_line_id);
CREATE INDEX IF NOT EXISTS idx_match_pairs_active ON reconciliation_match_pairs (reconciliation_id, active);
