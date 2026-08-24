-- ============================================================
-- catalog_clients.business_reg_no 필수화
-- ============================================================

ALTER TABLE catalog_clients
    ALTER COLUMN business_reg_no SET NOT NULL;