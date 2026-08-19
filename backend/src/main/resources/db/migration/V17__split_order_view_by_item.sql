-- ============================================================
-- statistics 모듈 — 다품목(cart) 주문 지원
--
-- order 모듈이 주문 하나에 여러 품목(items)을 담을 수 있도록 바뀌면서,
-- statistics_order_view도 "주문 1건 = row 1개"에서 "품목 1개 = row 1개"로 전환한다.
-- 같은 주문에 속한 row들은 order_id가 같고 product_id로 구분되며,
-- status/due_date/on_time 등 주문 단위 정보는 그 주문의 모든 row에 동일하게 중복 저장된다.
--
-- 전제: 한 주문 안에서 같은 품목은 한 번만 담긴다(order_id, product_id 조합이 유니크).
-- ============================================================

ALTER TABLE statistics_order_view DROP CONSTRAINT IF EXISTS statistics_order_view_order_id_key;

ALTER TABLE statistics_order_view ADD CONSTRAINT statistics_order_view_order_id_product_id_key
    UNIQUE (order_id, product_id);

-- 이제 주문 전체 합계가 아니라 이 row(품목) 하나의 금액이므로 이름을 맞춘다.
ALTER TABLE statistics_order_view RENAME COLUMN total_amount TO line_amount;
