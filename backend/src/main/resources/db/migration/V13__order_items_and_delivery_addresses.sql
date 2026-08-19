-- Order 애그리거트의 다중 품목
CREATE TABLE order_items (
    id                      BIGSERIAL    PRIMARY KEY,
    order_id                BIGINT       NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    line_number             INT,
    product_id              BIGINT       NOT NULL,
    product_name_snapshot   VARCHAR(200) NOT NULL,
    unit_price_snapshot     BIGINT       NOT NULL CHECK (unit_price_snapshot >= 0),
    quantity                INT          NOT NULL CHECK (quantity > 0),
    line_amount             BIGINT       NOT NULL CHECK (line_amount >= 0)
);

INSERT INTO order_items (
    order_id, line_number, product_id, product_name_snapshot,
    unit_price_snapshot, quantity, line_amount
)
SELECT id, 0, product_id, product_name_snapshot,
       unit_price_snapshot, quantity, unit_price_snapshot * quantity
FROM orders;

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

ALTER TABLE orders DROP COLUMN product_id;
ALTER TABLE orders DROP COLUMN product_name_snapshot;
ALTER TABLE orders DROP COLUMN unit_price_snapshot;
ALTER TABLE orders DROP COLUMN quantity;

-- Order가 소유하는 거래처별 주문용 배송지 목록
CREATE TABLE order_delivery_addresses (
    id                BIGSERIAL    PRIMARY KEY,
    client_id         BIGINT       NOT NULL,
    name              VARCHAR(100) NOT NULL,
    recipient_name    VARCHAR(100) NOT NULL DEFAULT '',
    recipient_phone   VARCHAR(50)  NOT NULL DEFAULT '',
    postal_code       VARCHAR(20)  NOT NULL DEFAULT '',
    address_line1     TEXT         NOT NULL,
    address_line2     TEXT         NOT NULL DEFAULT '',
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_order_delivery_addresses_client_id
    ON order_delivery_addresses(client_id, updated_at DESC);

-- 주문 당시 선택한 배송 정보 스냅샷
ALTER TABLE orders ADD COLUMN delivery_address_id BIGINT;
ALTER TABLE orders ADD COLUMN delivery_name_snapshot VARCHAR(100);
ALTER TABLE orders ADD COLUMN delivery_recipient_name_snapshot VARCHAR(100);
ALTER TABLE orders ADD COLUMN delivery_recipient_phone_snapshot VARCHAR(50);
ALTER TABLE orders ADD COLUMN delivery_postal_code_snapshot VARCHAR(20);
ALTER TABLE orders ADD COLUMN delivery_address_detail_snapshot TEXT;

UPDATE orders
SET delivery_address = COALESCE(NULLIF(BTRIM(delivery_address), ''), '미입력'),
    delivery_name_snapshot = '기존 배송지',
    delivery_recipient_name_snapshot = client_name_snapshot,
    delivery_recipient_phone_snapshot = COALESCE(client_phone_snapshot, ''),
    delivery_postal_code_snapshot = '',
    delivery_address_detail_snapshot = '';

ALTER TABLE orders ALTER COLUMN delivery_address SET NOT NULL;
ALTER TABLE orders ALTER COLUMN delivery_name_snapshot SET NOT NULL;
ALTER TABLE orders ALTER COLUMN delivery_recipient_name_snapshot SET NOT NULL;
ALTER TABLE orders ALTER COLUMN delivery_recipient_phone_snapshot SET NOT NULL;
ALTER TABLE orders ALTER COLUMN delivery_postal_code_snapshot SET NOT NULL;
ALTER TABLE orders ALTER COLUMN delivery_address_detail_snapshot SET NOT NULL;
