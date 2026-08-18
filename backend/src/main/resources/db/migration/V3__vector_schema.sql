-- pgvector 확장 활성화
CREATE EXTENSION IF NOT EXISTS vector;

-- ============================================================
-- catalog 모듈 — 거래처/품목 비정형 메모 추가
-- ============================================================

ALTER TABLE catalog_clients  ADD COLUMN memo TEXT;
ALTER TABLE catalog_products ADD COLUMN memo TEXT;

-- ============================================================
-- production 모듈 — 생산/배송 비정형 메모 추가
-- ============================================================

ALTER TABLE production_records ADD COLUMN memo TEXT;

-- ============================================================
-- statistics 모듈 — 임베딩 테이블
-- 각 도메인 이벤트 구독 후 statistics가 임베딩 생성/저장
-- ============================================================

-- 수주 메모 (orders.note)
CREATE TABLE statistics_note_embeddings (
    id         BIGSERIAL    PRIMARY KEY,
    order_id   BIGINT       NOT NULL UNIQUE,
    note_text  TEXT         NOT NULL,
    embedding  vector(768),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- 생산 메모 (production_records.memo)
CREATE TABLE statistics_production_embeddings (
    id                   BIGSERIAL    PRIMARY KEY,
    production_record_id BIGINT       NOT NULL UNIQUE,
    memo_text            TEXT         NOT NULL,
    embedding            vector(768),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- 거래처 메모 (catalog_clients.memo)
CREATE TABLE statistics_client_embeddings (
    id         BIGSERIAL    PRIMARY KEY,
    client_id  BIGINT       NOT NULL UNIQUE,
    memo_text  TEXT         NOT NULL,
    embedding  vector(768),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_note_embeddings_order_id        ON statistics_note_embeddings (order_id);
CREATE INDEX idx_note_embeddings_vector          ON statistics_note_embeddings        USING ivfflat (embedding vector_cosine_ops);

CREATE INDEX idx_production_embeddings_record_id ON statistics_production_embeddings (production_record_id);
CREATE INDEX idx_production_embeddings_vector    ON statistics_production_embeddings  USING ivfflat (embedding vector_cosine_ops);

CREATE INDEX idx_client_embeddings_client_id     ON statistics_client_embeddings (client_id);
CREATE INDEX idx_client_embeddings_vector        ON statistics_client_embeddings      USING ivfflat (embedding vector_cosine_ops);
