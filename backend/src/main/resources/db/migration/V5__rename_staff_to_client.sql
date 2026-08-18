-- ============================================================
-- user_role_codes — STAFF → CLIENT 리네임
-- ============================================================

ALTER TABLE user_role_codes
    DROP CONSTRAINT user_role_codes_code_check;

UPDATE user_role_codes SET code = 'CLIENT' WHERE code = 'STAFF';

ALTER TABLE user_role_codes
    ADD CONSTRAINT user_role_codes_code_check
        CHECK (code IN ('CLIENT', 'ADMIN'));
