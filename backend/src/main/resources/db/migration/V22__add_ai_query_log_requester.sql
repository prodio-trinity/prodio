-- ============================================================
-- statistics 모듈 — AI 요약/질의 요청자 추가
-- 관리자 개인은 본인이 요청한 이력만 조회할 수 있어야 해서, 요청한 관리자를 식별할 컬럼이 필요하다.
-- ============================================================

ALTER TABLE statistics_ai_query_log ADD COLUMN requested_by BIGINT NOT NULL;

CREATE INDEX idx_ai_query_log_requester ON statistics_ai_query_log (requested_by, query_type, requested_at DESC);
