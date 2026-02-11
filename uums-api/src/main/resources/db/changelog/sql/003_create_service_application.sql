--liquibase formatted sql

--changeset uums:003-create-service-application
CREATE TABLE IF NOT EXISTS service_application (
    id BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(160) NOT NULL UNIQUE,
    owner_email VARCHAR(255) NOT NULL,
    api_key VARCHAR(128) NOT NULL UNIQUE,
    pricing_tier VARCHAR(30) NOT NULL,
    request_limit_per_min INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
