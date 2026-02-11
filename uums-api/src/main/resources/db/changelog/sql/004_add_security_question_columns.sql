--liquibase formatted sql

--changeset uums:004-add-security-question-columns
ALTER TABLE uums_user
    ADD COLUMN IF NOT EXISTS security_question VARCHAR(255);

--changeset uums:004-add-security-answer-hash
ALTER TABLE uums_user
    ADD COLUMN IF NOT EXISTS security_answer_hash VARCHAR(255);
