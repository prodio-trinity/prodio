-- ============================================================
-- catalog 모듈 — 거래처 & 품목
-- ============================================================

CREATE TABLE catalog_clients (
    id              BIGSERIAL    PRIMARY KEY,
    company_name    VARCHAR(200) NOT NULL,
    representative  VARCHAR(100),
    address         TEXT,
    phone           VARCHAR(50),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE catalog_products (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    unit_price  BIGINT       NOT NULL CHECK (unit_price >= 0),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_catalog_clients_name ON catalog_clients (company_name);
CREATE INDEX idx_catalog_products_name ON catalog_products (name);

-- ============================================================
-- order 모듈 — 수주
-- (catalog 테이블에 FK 없음 — 모듈 경계 준수)
-- ============================================================

CREATE TABLE orders (
    id                      BIGSERIAL    PRIMARY KEY,

    -- 거래처 참조 (ID만, FK 없음)
    client_id               BIGINT       NOT NULL,
    client_name_snapshot    VARCHAR(200) NOT NULL,  -- 수주 시점 회사명
    client_phone_snapshot   VARCHAR(50),             -- 수주 시점 연락처 (SMS용)

    -- 품목 참조 (ID만, FK 없음)
    product_id              BIGINT       NOT NULL,
    product_name_snapshot   VARCHAR(200) NOT NULL,  -- 수주 시점 품목명
    unit_price_snapshot     BIGINT       NOT NULL,  -- 수주 시점 단가

    quantity                INT          NOT NULL CHECK (quantity > 0),
    vat_included            BOOLEAN      NOT NULL DEFAULT false,
    total_amount            BIGINT       NOT NULL CHECK (total_amount >= 0),

    due_date                DATE         NOT NULL,
    delivery_address        TEXT,
    note                    TEXT,

    status                  VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'IN_PRODUCTION')),

    payment_confirmed       BOOLEAN      NOT NULL DEFAULT false,

    -- 등록자 참조 (ID만, FK 없음)
    created_by              BIGINT       NOT NULL,

    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_status     ON orders (status);
CREATE INDEX idx_orders_due_date   ON orders (due_date);
CREATE INDEX idx_orders_client_id  ON orders (client_id);
CREATE INDEX idx_orders_created_at ON orders (created_at);

-- ============================================================
-- production 모듈 — 생산 기록
-- (orders 테이블에 FK 없음 — 모듈 경계 준수)
-- ============================================================

CREATE TABLE production_records (
    id            BIGSERIAL   PRIMARY KEY,

    -- 수주 참조 (ID만, FK 없음)
    order_id      BIGINT      NOT NULL UNIQUE,  -- 수주당 생산 기록 1건

    status        VARCHAR(20) NOT NULL DEFAULT 'IN_PRODUCTION'
        CHECK (status IN ('IN_PRODUCTION', 'IN_DELIVERY', 'COMPLETED')),

    started_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    shipped_at    TIMESTAMPTZ,
    completed_at  TIMESTAMPTZ,

    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_production_records_status   ON production_records (status);
CREATE INDEX idx_production_records_order_id ON production_records (order_id);

-- ============================================================
-- statistics 모듈 — 조회 전용 Read Model
-- 이벤트(OrderCreated / OrderShipped / OrderCompleted) 구독으로 갱신
-- ============================================================

CREATE TABLE statistics_order_view (
    id                      BIGSERIAL    PRIMARY KEY,
    order_id                BIGINT       NOT NULL UNIQUE,

    -- 스냅샷 (이벤트 페이로드에서 복사)
    client_id               BIGINT       NOT NULL,
    client_name             VARCHAR(200) NOT NULL,
    product_id              BIGINT       NOT NULL,
    product_name            VARCHAR(200) NOT NULL,

    quantity                INT          NOT NULL,
    total_amount            BIGINT       NOT NULL,
    due_date                DATE         NOT NULL,

    -- 전체 생애 상태 (PENDING ~ COMPLETED)
    status                  VARCHAR(20)  NOT NULL
        CHECK (status IN ('PENDING', 'IN_PRODUCTION', 'IN_DELIVERY', 'COMPLETED')),

    payment_confirmed       BOOLEAN      NOT NULL DEFAULT false,

    -- 납기 이행 여부 (COMPLETED 시 confirmed_at <= due_date 이면 true)
    on_time                 BOOLEAN,

    -- 각 단계 타임스탬프
    order_created_at        TIMESTAMPTZ  NOT NULL,
    production_started_at   TIMESTAMPTZ,
    shipped_at              TIMESTAMPTZ,
    completed_at            TIMESTAMPTZ
);

CREATE INDEX idx_stats_view_status       ON statistics_order_view (status);
CREATE INDEX idx_stats_view_due_date     ON statistics_order_view (due_date);
CREATE INDEX idx_stats_view_created_at   ON statistics_order_view (order_created_at);
CREATE INDEX idx_stats_view_product_id   ON statistics_order_view (product_id);
CREATE INDEX idx_stats_view_client_id    ON statistics_order_view (client_id);
