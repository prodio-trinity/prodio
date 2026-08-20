-- ============================================================
-- statistics 모듈 — NOTE 네이밍을 ORDER 기준으로 정리
-- production/client 임베딩 테이블(production_embeddings/client_embeddings)과 동일하게
-- 소유 도메인 이름을 접두어로 맞춘다. NOTE -> ORDER_NOTE도 같은 이유.
-- ============================================================

ALTER TABLE statistics_note_embeddings RENAME TO statistics_order_embeddings;

ALTER TABLE statistics_order_embeddings RENAME CONSTRAINT statistics_note_embeddings_pkey
    TO statistics_order_embeddings_pkey;
ALTER TABLE statistics_order_embeddings RENAME CONSTRAINT statistics_note_embeddings_order_id_key
    TO statistics_order_embeddings_order_id_key;
ALTER SEQUENCE statistics_note_embeddings_id_seq RENAME TO statistics_order_embeddings_id_seq;

ALTER INDEX idx_note_embeddings_order_id RENAME TO idx_order_embeddings_order_id;
ALTER INDEX idx_note_embeddings_vector RENAME TO idx_order_embeddings_vector;

ALTER TABLE statistics_ai_query_log DROP CONSTRAINT statistics_ai_query_log_source_type_check;
ALTER TABLE statistics_ai_query_log ADD CONSTRAINT statistics_ai_query_log_source_type_check
    CHECK (source_type IN ('ORDER_NOTE', 'CLIENT_MEMO', 'PRODUCTION_MEMO', 'ALL'));
