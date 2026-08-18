-- ============================================================
-- 1. catalog_clients
-- ============================================================

-- 1-1. 컬럼명 정리: representative(대표자) -> ceo_name
ALTER TABLE catalog_clients
    RENAME COLUMN representative TO ceo_name;

-- 1-2. 신규 컬럼 추가

ALTER TABLE catalog_clients
    ADD COLUMN client_code     VARCHAR(20),
    ADD COLUMN business_reg_no VARCHAR(20),
    ADD COLUMN manager_name    VARCHAR(100),
    ADD COLUMN is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN user_id         BIGINT;

-- 1-3. client_code 채번용 시퀀스

CREATE SEQUENCE catalog_client_code_seq START WITH 1;

-- 1-4. 제약 추가

ALTER TABLE catalog_clients
    ALTER COLUMN client_code SET NOT NULL,
    ADD CONSTRAINT uk_catalog_clients_client_code UNIQUE (client_code),
    ADD CONSTRAINT uk_catalog_clients_business_reg_no UNIQUE (business_reg_no),
    ADD CONSTRAINT uk_catalog_clients_user_id UNIQUE (user_id);


-- ============================================================
-- 2. catalog_product_categories
-- ============================================================

-- 2단 대/소분류 테이블.
-- 시드 데이터(대/소분류 INSERT)는 별도 V3__seed_product_categories.sql에서 관리.

CREATE TABLE catalog_product_categories (
    id            BIGSERIAL PRIMARY KEY,
    category_code VARCHAR(20) NOT NULL UNIQUE,
    category_name VARCHAR(50) NOT NULL,
    parent_id     BIGINT REFERENCES catalog_product_categories (id),
    is_active     BOOLEAN NOT NULL DEFAULT TRUE
);

-- 대분류별 품목코드 채번 시퀀스.

CREATE SEQUENCE catalog_product_code_seq_input     START WITH 1;
CREATE SEQUENCE catalog_product_code_seq_display   START WITH 1;
CREATE SEQUENCE catalog_product_code_seq_storage   START WITH 1;
CREATE SEQUENCE catalog_product_code_seq_computing START WITH 1;
CREATE SEQUENCE catalog_product_code_seq_audio     START WITH 1;
CREATE SEQUENCE catalog_product_code_seq_accessory START WITH 1;


-- ============================================================
-- 3. catalog_products
-- ============================================================

-- 3-1. 컬럼명 정리: name -> product_name

ALTER TABLE catalog_products
    RENAME COLUMN name TO product_name;

-- 3-2. 신규 컬럼 추가

ALTER TABLE catalog_products
    ADD COLUMN product_code VARCHAR(20),
    ADD COLUMN category_id  BIGINT,
    ADD COLUMN unit         VARCHAR(10) NOT NULL DEFAULT 'EA',
    ADD COLUMN is_active    BOOLEAN     NOT NULL DEFAULT TRUE;

-- 3-3. 컬럼 수정

ALTER TABLE catalog_products
    ALTER COLUMN product_code SET NOT NULL,
ALTER COLUMN category_id SET NOT NULL,
    ADD CONSTRAINT uk_catalog_products_product_code UNIQUE (product_code),
    ADD CONSTRAINT fk_catalog_products_category
        FOREIGN KEY (category_id) REFERENCES catalog_product_categories (id),
    ADD CONSTRAINT ck_catalog_products_unit
        CHECK (unit IN ('EA', 'BOX', 'KG', 'SET'));

CREATE INDEX idx_catalog_products_category_id
    ON catalog_products (category_id);
