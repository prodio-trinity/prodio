-- ============================================================
-- statistics 모듈 — AI 요약/질의응답 이력
-- STATS_SUMMARY: 정형 통계 자연어 요약, RAG_QA: note/메모 기반 질의응답
-- ============================================================

CREATE TABLE statistics_ai_query_log (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),

    query_type   VARCHAR(20)  NOT NULL
        CHECK (query_type IN ('STATS_SUMMARY', 'RAG_QA')),

    -- RAG_QA일 때만 사용 (검색 대상 소스)
    source_type  VARCHAR(20)
        CHECK (source_type IN ('NOTE', 'CLIENT_MEMO', 'PRODUCTION_MEMO', 'ALL')),

    question     VARCHAR(500) NOT NULL,
    response     TEXT         NOT NULL,
    requested_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_ai_query_log_type_requested_at
    ON statistics_ai_query_log (query_type, requested_at DESC);
