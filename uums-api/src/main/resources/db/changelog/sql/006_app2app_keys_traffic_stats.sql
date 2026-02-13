--liquibase formatted sql

--changeset uums:006-create-service-api-key
CREATE TABLE IF NOT EXISTS service_api_key (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL,
    key_name VARCHAR(120) NOT NULL,
    api_key VARCHAR(128) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_service_api_key_service FOREIGN KEY (service_id) REFERENCES service_application (id)
);

--changeset uums:006-seed-default-keys
INSERT INTO service_api_key (service_id, key_name, api_key, active)
SELECT sa.id, 'default', sa.api_key, TRUE
FROM service_application sa
WHERE NOT EXISTS (
    SELECT 1 FROM service_api_key sk WHERE sk.api_key = sa.api_key
);

--changeset uums:006-create-service-traffic-event
CREATE TABLE IF NOT EXISTS service_traffic_event (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL,
    api_name VARCHAR(160) NOT NULL,
    input_payload TEXT NOT NULL,
    status VARCHAR(40) NOT NULL,
    event_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_service_traffic_service FOREIGN KEY (service_id) REFERENCES service_application (id)
);

--changeset uums:006-create-service-stat-snapshot
CREATE TABLE IF NOT EXISTS service_stat_snapshot (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL,
    stat_code VARCHAR(80) NOT NULL,
    stat_value BIGINT NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_service_stat_code UNIQUE (service_id, stat_code),
    CONSTRAINT fk_service_stat_service FOREIGN KEY (service_id) REFERENCES service_application (id)
);

--changeset uums:006-seed-service-stats
INSERT INTO service_stat_snapshot (service_id, stat_code, stat_value)
SELECT sa.id, 'REQUEST_COUNT', 0
FROM service_application sa
WHERE NOT EXISTS (
    SELECT 1 FROM service_stat_snapshot ss WHERE ss.service_id = sa.id AND ss.stat_code = 'REQUEST_COUNT'
);
