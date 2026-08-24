-- ============================================================
-- statistics 모듈 — 임베딩 벡터 인덱스를 ivfflat에서 hnsw로 교체
-- ivfflat은 lists(클러스터) 개수를 데이터 규모에 맞춰 미리 정해야 하는데,
-- V3에서 lists를 지정하지 않고 만들어 초기(테이블당 수십 건) 단계에서
-- 특정 쿼리 벡터가 가리키는 클러스터가 비어 검색 결과가 통째로 0건이 되는
-- 문제가 로컬 RAG QA 테스트 중 확인됐다. hnsw는 그래프 기반이라 데이터
-- 규모와 무관하게 lists 튜닝 없이 안정적인 recall을 보장한다.
-- ============================================================

DROP INDEX idx_order_embeddings_vector;
DROP INDEX idx_production_embeddings_vector;
DROP INDEX idx_client_embeddings_vector;

CREATE INDEX idx_order_embeddings_vector
    ON statistics_order_embeddings USING hnsw (embedding vector_cosine_ops);
CREATE INDEX idx_production_embeddings_vector
    ON statistics_production_embeddings USING hnsw (embedding vector_cosine_ops);
CREATE INDEX idx_client_embeddings_vector
    ON statistics_client_embeddings USING hnsw (embedding vector_cosine_ops);
