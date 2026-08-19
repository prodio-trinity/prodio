-- ============================================================
-- statistics 모듈 — order 모듈의 CANCELLED 상태 반영
-- OrderCancelledEvent(cancellationReason, cancelledAt) 대응
-- ============================================================

ALTER TABLE statistics_order_view DROP CONSTRAINT IF EXISTS statistics_order_view_status_check;

ALTER TABLE statistics_order_view ADD COLUMN cancellation_reason TEXT;
ALTER TABLE statistics_order_view ADD COLUMN cancelled_at TIMESTAMPTZ;

ALTER TABLE statistics_order_view ADD CONSTRAINT statistics_order_view_status_check
    CHECK (status IN ('PENDING', 'IN_PRODUCTION', 'IN_DELIVERY', 'COMPLETED', 'CANCELLED'));

ALTER TABLE statistics_order_view ADD CONSTRAINT statistics_order_view_cancellation_reason_check
    CHECK (
        (status = 'CANCELLED' AND NULLIF(BTRIM(cancellation_reason), '') IS NOT NULL)
        OR (status <> 'CANCELLED' AND cancellation_reason IS NULL)
    );
