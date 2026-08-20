-- ============================================================
-- catalog 모듈 — 거래처 등록 신청 
-- 사용자가 본인 회사 정보를 등록 신청, ADMIN이 승인/반려. 
-- ============================================================

CREATE TABLE catalog_client_registration_requests (
    id               BIGSERIAL    PRIMARY KEY,
    user_id          BIGINT       NOT NULL,
    company_name     VARCHAR(200) NOT NULL,
    ceo_name         VARCHAR(100),
    business_reg_no  VARCHAR(20)  NOT NULL,
    phone            VARCHAR(50),
    address          TEXT,
    manager_name     VARCHAR(100),
    status           VARCHAR(20)  NOT NULL
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    reject_reason    TEXT,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    reviewed_at      TIMESTAMPTZ,
    CONSTRAINT uk_catalog_client_registration_requests_user_id UNIQUE (user_id)
);

CREATE INDEX idx_catalog_client_registration_requests_status
    ON catalog_client_registration_requests (status);