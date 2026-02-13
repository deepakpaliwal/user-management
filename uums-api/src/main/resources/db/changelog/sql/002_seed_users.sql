--liquibase formatted sql

--changeset uums:002-seed-role-admin
INSERT INTO role (role_code, role_name)
SELECT 'ROLE_ADMIN', 'Administrator'
WHERE NOT EXISTS (
    SELECT 1 FROM role WHERE role_code = 'ROLE_ADMIN'
);

--changeset uums:002-seed-role-user
INSERT INTO role (role_code, role_name)
SELECT 'ROLE_USER', 'Standard User'
WHERE NOT EXISTS (
    SELECT 1 FROM role WHERE role_code = 'ROLE_USER'
);

--changeset uums:002-seed-user-admin
INSERT INTO uums_user (username, email, password_hash, account_status)
SELECT 'admin_uums', 'admin@uums.local', '{noop}SecureAdmin@2026', 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1 FROM uums_user WHERE username = 'admin_uums'
);

--changeset uums:002-seed-user-test
INSERT INTO uums_user (username, email, password_hash, account_status)
SELECT 'test_user_01', 'test@uums.local', '{noop}Password@123', 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1 FROM uums_user WHERE username = 'test_user_01'
);

--changeset uums:002-map-admin-role
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM uums_user u
JOIN role r ON r.role_code = 'ROLE_ADMIN'
WHERE u.username = 'admin_uums'
  AND NOT EXISTS (
      SELECT 1
      FROM user_role ur
      WHERE ur.user_id = u.id
        AND ur.role_id = r.id
  );

--changeset uums:002-map-test-role
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM uums_user u
JOIN role r ON r.role_code = 'ROLE_USER'
WHERE u.username = 'test_user_01'
  AND NOT EXISTS (
      SELECT 1
      FROM user_role ur
      WHERE ur.user_id = u.id
        AND ur.role_id = r.id
  );
