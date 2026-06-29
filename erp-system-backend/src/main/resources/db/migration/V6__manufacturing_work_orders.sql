-- Manufacturing: work orders

CREATE TABLE IF NOT EXISTS work_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL REFERENCES products (id),
    warehouse_id BIGINT REFERENCES warehouses (id),
    quantity NUMERIC(19, 4) NOT NULL,
    produced_quantity NUMERIC(19, 4) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    planned_start DATE,
    planned_end DATE,
    notes VARCHAR(500),
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_work_orders_status CHECK (status IN ('PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_work_orders_status ON work_orders (status);
CREATE INDEX IF NOT EXISTS idx_work_orders_product ON work_orders (product_id);

INSERT INTO ui_menu_items (id, parent_id, sort_order, item_type, title_key, icon, url, is_external, target_blank, roles_csv, item_classes, breadcrumbs_flag)
VALUES
('erp-manufacturing-orders', 'erp-manufacturing', 1, 'item', 'MENU.WORK_ORDERS', 'build', '/manufacturing', FALSE, FALSE, 'ADMIN,ACCOUNTANT_STANDARD,MANAGER', NULL, TRUE)
ON CONFLICT (id) DO UPDATE SET url = EXCLUDED.url, item_type = EXCLUDED.item_type, title_key = EXCLUDED.title_key, icon = EXCLUDED.icon;

INSERT INTO role_menu_permissions (role_id, menu_item_id, can_view, can_create, can_edit, can_delete, created_by, updated_by)
SELECT r.id, m.id, TRUE, TRUE, TRUE, TRUE, 'system', 'system'
FROM access_roles r
CROSS JOIN ui_menu_items m
WHERE r.code IN ('ADMIN', 'MANAGER', 'ACCOUNTANT_STANDARD')
  AND m.id = 'erp-manufacturing-orders'
  AND NOT EXISTS (
      SELECT 1 FROM role_menu_permissions rp
      WHERE rp.role_id = r.id AND rp.menu_item_id = m.id
  );
