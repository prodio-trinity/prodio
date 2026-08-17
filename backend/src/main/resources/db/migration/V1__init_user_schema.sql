-- ============================================
-- User 도메인 초기 스키마
-- ============================================

CREATE TABLE user_role_codes (
    id   BIGINT PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE
        CHECK (code IN ('STAFF', 'ADMIN'))
);

INSERT INTO user_role_codes (id, code) VALUES
    (1, 'STAFF'),
    (2, 'ADMIN');

CREATE TABLE user_accounts (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name          VARCHAR(100) NOT NULL DEFAULT '',
    status        VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES user_accounts (id),
    role_id BIGINT NOT NULL REFERENCES user_role_codes (id),
    PRIMARY KEY (user_id, role_id)
);

-- 초기 관리자 계정 (비밀번호: admin1234 / BCrypt cost 8)
INSERT INTO user_accounts (email, password_hash, name)
VALUES ('admin@prodio.com', '$2a$08$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '관리자');

INSERT INTO user_roles (user_id, role_id)
VALUES (1, 2);
