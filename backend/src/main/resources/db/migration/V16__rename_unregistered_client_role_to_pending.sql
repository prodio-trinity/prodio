ALTER TABLE user_role_codes
    DROP CONSTRAINT user_role_codes_code_check;

UPDATE user_role_codes
SET code = 'PENDING'
WHERE code = 'UNREGISTERED_CLIENT';

ALTER TABLE user_role_codes
    ADD CONSTRAINT user_role_codes_code_check
        CHECK (code IN ('PENDING', 'CLIENT', 'ADMIN'));
