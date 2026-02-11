--liquibase formatted sql

--changeset uums:005-create-user-stat-snapshot
CREATE TABLE IF NOT EXISTS user_stat_snapshot (
    id BIGSERIAL PRIMARY KEY,
    metric_code VARCHAR(80) NOT NULL,
    metric_value INT NOT NULL,
    snapshot_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--changeset uums:005-seed-role-developer
INSERT INTO role (role_code, role_name)
SELECT 'ROLE_DEVELOPER', 'Developer'
WHERE NOT EXISTS (
    SELECT 1 FROM role WHERE role_code = 'ROLE_DEVELOPER'
);

--changeset uums:005-seed-user-analytics
INSERT INTO uums_user (username, email, password_hash, account_status)
SELECT 'analytics_user', 'analytics@uums.local', '{noop}Analytics@123', 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1 FROM uums_user WHERE username = 'analytics_user'
);

--changeset uums:005-seed-user-dev
INSERT INTO uums_user (username, email, password_hash, account_status)
SELECT 'dev_portal', 'dev@uums.local', '{noop}DevPortal@123', 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1 FROM uums_user WHERE username = 'dev_portal'
);

--changeset uums:005-map-analytics-role
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM uums_user u
JOIN role r ON r.role_code = 'ROLE_USER'
WHERE u.username = 'analytics_user'
  AND NOT EXISTS (
      SELECT 1 FROM user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

--changeset uums:005-map-dev-role
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM uums_user u
JOIN role r ON r.role_code = 'ROLE_DEVELOPER'
WHERE u.username = 'dev_portal'
  AND NOT EXISTS (
      SELECT 1 FROM user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

--changeset uums:005-seed-service-portal
INSERT INTO service_application (service_name, owner_email, api_key, pricing_tier, request_limit_per_min, active)
SELECT 'portal-service', 'owner@uums.local', 'uums_sample_portal_001', 'BASIC', 300, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM service_application WHERE service_name = 'portal-service'
);

--changeset uums:005-seed-service-analytics
INSERT INTO service_application (service_name, owner_email, api_key, pricing_tier, request_limit_per_min, active)
SELECT 'analytics-service', 'analytics@uums.local', 'uums_sample_analytics_001', 'PRO', 1000, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM service_application WHERE service_name = 'analytics-service'
);

--changeset uums:005-seed-user-stats-active
INSERT INTO user_stat_snapshot (metric_code, metric_value)
SELECT 'ACTIVE_USERS', 12480
WHERE NOT EXISTS (
    SELECT 1 FROM user_stat_snapshot WHERE metric_code = 'ACTIVE_USERS'
);

--changeset uums:005-seed-user-stats-mfa
INSERT INTO user_stat_snapshot (metric_code, metric_value)
SELECT 'MFA_ENABLED_USERS', 8940
WHERE NOT EXISTS (
    SELECT 1 FROM user_stat_snapshot WHERE metric_code = 'MFA_ENABLED_USERS'
);

--changeset uums:005-seed-user-stats-services
INSERT INTO user_stat_snapshot (metric_code, metric_value)
SELECT 'ONBOARDED_SERVICES', 214
WHERE NOT EXISTS (
    SELECT 1 FROM user_stat_snapshot WHERE metric_code = 'ONBOARDED_SERVICES'
);
