--liquibase formatted sql

--changeset uums:002-seed-roles
INSERT INTO role (role_code, role_name)
VALUES ('ROLE_ADMIN', 'Administrator'),
       ('ROLE_USER', 'Standard User')
ON CONFLICT (role_code) DO NOTHING;

--changeset uums:002-seed-users
INSERT INTO uums_user (username, email, password_hash, account_status)
VALUES ('admin_uums', 'admin@uums.local', '{noop}SecureAdmin@2026', 'ACTIVE'),
       ('test_user_01', 'test@uums.local', '{noop}Password@123', 'ACTIVE')
ON CONFLICT (username) DO NOTHING;

--changeset uums:002-map-user-roles
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM uums_user u
         JOIN role r ON (u.username = 'admin_uums' AND r.role_code = 'ROLE_ADMIN')
             OR (u.username = 'test_user_01' AND r.role_code = 'ROLE_USER')
ON CONFLICT DO NOTHING;
