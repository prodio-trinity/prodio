-- ============================================================
-- 1. catalog_products -> catalog_product_categories 연결 해제
-- ============================================================

ALTER TABLE catalog_products
    DROP CONSTRAINT fk_catalog_products_category;

DROP INDEX IF EXISTS idx_catalog_products_category_id;

ALTER TABLE catalog_products
    DROP COLUMN category_id;

-- ============================================================
-- 2. 기존 2단(대/소분류 self-reference) 카테고리 테이블 제거
-- ============================================================

DROP TABLE catalog_product_categories;

-- ============================================================
-- 3. catalog_sub_categories (소분류만 저장, 대분류는 top_category 컬럼)
-- ============================================================

CREATE TABLE catalog_sub_categories
(
    id                bigserial primary key,
    sub_category_code varchar(20)  not null,
    name              varchar(100) not null,
    top_category      varchar(30)  not null,
    is_active         boolean      not null default true,
    created_at        timestamp with time zone not null default now(),
    updated_at        timestamp with time zone not null default now(),
    constraint uq_catalog_sub_categories_code
        unique (sub_category_code),
    constraint uq_catalog_sub_categories_name
        unique (top_category, name),
    constraint ck_catalog_sub_categories_top_category
        check (top_category in ('INPUT', 'DISPLAY', 'STORAGE', 'COMPUTING', 'AUDIO', 'ACCESSORY'))
);

-- ============================================================
-- 4. catalog_products.sub_category_id
-- ============================================================

ALTER TABLE catalog_products
    ADD COLUMN sub_category_id bigint NOT NULL,
    ADD CONSTRAINT fk_catalog_products_sub_category
        FOREIGN KEY (sub_category_id) REFERENCES catalog_sub_categories (id);

CREATE INDEX idx_catalog_products_sub_category_id
    ON catalog_products (sub_category_id);

-- ============================================================
-- 5. catalog_products.unit_price: BIGINT -> NUMERIC(14,2)
-- ============================================================

ALTER TABLE catalog_products
    ALTER COLUMN unit_price TYPE numeric(14, 2);