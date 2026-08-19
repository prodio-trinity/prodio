ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check;

ALTER TABLE orders ADD COLUMN cancellation_reason TEXT;

UPDATE orders
SET status = CASE
    WHEN payment_confirmed THEN 'CONFIRMED'
    ELSE 'PENDING_PAYMENT'
END;

ALTER TABLE orders ALTER COLUMN status SET DEFAULT 'PENDING_PAYMENT';
ALTER TABLE orders ADD CONSTRAINT orders_status_check
    CHECK (status IN ('PENDING_PAYMENT', 'CONFIRMED', 'CANCELLED'));
ALTER TABLE orders ADD CONSTRAINT orders_cancellation_reason_check
    CHECK (
        (status = 'CANCELLED' AND NULLIF(BTRIM(cancellation_reason), '') IS NOT NULL)
        OR (status <> 'CANCELLED' AND cancellation_reason IS NULL)
    );

ALTER TABLE orders DROP COLUMN payment_confirmed;
