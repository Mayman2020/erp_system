-- Open fiscal year covering current demo operations (2026).

INSERT INTO fiscal_years (year, start_date, end_date, is_open, created_by, updated_by)
SELECT 2026, DATE '2026-01-01', DATE '2026-12-31', TRUE, 'flyway', 'flyway'
WHERE NOT EXISTS (SELECT 1 FROM fiscal_years WHERE year = 2026);

INSERT INTO fiscal_periods (fiscal_year_id, period_name, start_date, end_date, is_open, created_by, updated_by)
SELECT fy.id, '2026', fy.start_date, fy.end_date, TRUE, 'flyway', 'flyway'
FROM fiscal_years fy
WHERE fy.year = 2026
  AND NOT EXISTS (
      SELECT 1 FROM fiscal_periods fp WHERE fp.fiscal_year_id = fy.id AND fp.period_name = '2026'
  );
