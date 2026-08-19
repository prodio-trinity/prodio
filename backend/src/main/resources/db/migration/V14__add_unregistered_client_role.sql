ALTER TABLE user_role_codes
    DROP CONSTRAINT user_role_codes_code_check;

ALTER TABLE user_role_codes
    ADD CONSTRAINT user_role_codes_code_check
        CHECK (code IN ('UNREGISTERED_CLIENT', 'CLIENT', 'ADMIN'));

INSERT INTO user_role_codes (id, code)
VALUES (3, 'UNREGISTERED_CLIENT')
ON CONFLICT (code) DO NOTHING;
