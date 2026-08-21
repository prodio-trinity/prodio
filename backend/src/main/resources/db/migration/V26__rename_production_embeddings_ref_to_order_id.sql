-- ============================================================
-- statistics 모듈 — production_embeddings의 참조 키를 order_id로 정리
-- 실제로 구독하는 ProductionMemo(orderId, memo) 이벤트엔 production_record_id가
-- 없다. production_records가 order_id UNIQUE(주문당 생산 기록 1건)라 의미상
-- 문제없고, stat은 production 모듈의 PK를 알 방법이 없어 orderId로 통일한다.
-- ============================================================

ALTER TABLE statistics_production_embeddings RENAME COLUMN production_record_id TO order_id;

ALTER TABLE statistics_production_embeddings RENAME CONSTRAINT statistics_production_embeddings_production_record_id_key
    TO statistics_production_embeddings_order_id_key;

ALTER INDEX idx_production_embeddings_record_id RENAME TO idx_production_embeddings_order_id;
