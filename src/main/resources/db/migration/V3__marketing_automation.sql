CREATE TABLE email_providers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    provider_type VARCHAR(32) NOT NULL,
    host VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    from_email VARCHAR(255) NOT NULL,
    from_name VARCHAR(255),
    reply_to VARCHAR(255),
    tls_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE segments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    color VARCHAR(32),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE contact_segments (
    contact_id BIGINT NOT NULL REFERENCES contacts (id) ON DELETE CASCADE,
    segment_id BIGINT NOT NULL REFERENCES segments (id) ON DELETE CASCADE,
    PRIMARY KEY (contact_id, segment_id)
);

CREATE INDEX idx_contact_segments_segment_id ON contact_segments (segment_id);

CREATE TABLE email_campaigns (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    preview_text VARCHAR(255),
    sender_name VARCHAR(255),
    html_content TEXT NOT NULL,
    plain_text_content TEXT,
    status VARCHAR(32) NOT NULL,
    provider_id BIGINT REFERENCES email_providers (id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE campaign_segments (
    campaign_id BIGINT NOT NULL REFERENCES email_campaigns (id) ON DELETE CASCADE,
    segment_id BIGINT NOT NULL REFERENCES segments (id) ON DELETE CASCADE,
    PRIMARY KEY (campaign_id, segment_id)
);

CREATE INDEX idx_campaign_segments_segment_id ON campaign_segments (segment_id);

CREATE TABLE campaign_deliveries (
    id BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT NOT NULL REFERENCES email_campaigns (id) ON DELETE CASCADE,
    contact_id BIGINT NOT NULL REFERENCES contacts (id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL,
    failure_reason VARCHAR(1000),
    external_message_id VARCHAR(255),
    sent_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_campaign_deliveries_campaign_id ON campaign_deliveries (campaign_id);
CREATE UNIQUE INDEX idx_campaign_deliveries_campaign_contact ON campaign_deliveries (campaign_id, contact_id);

CREATE TABLE forms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    headline VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    submit_label VARCHAR(64) NOT NULL,
    success_title VARCHAR(255) NOT NULL,
    success_message VARCHAR(1000) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    target_segment_id BIGINT REFERENCES segments (id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE form_questions (
    id BIGSERIAL PRIMARY KEY,
    form_id BIGINT NOT NULL REFERENCES forms (id) ON DELETE CASCADE,
    field_key VARCHAR(120) NOT NULL,
    label VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    placeholder VARCHAR(255),
    question_type VARCHAR(32) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT TRUE,
    position_index INTEGER NOT NULL,
    options_json TEXT
);

CREATE INDEX idx_form_questions_form_id ON form_questions (form_id);

CREATE TABLE form_responses (
    id BIGSERIAL PRIMARY KEY,
    form_id BIGINT NOT NULL REFERENCES forms (id) ON DELETE CASCADE,
    contact_id BIGINT REFERENCES contacts (id),
    respondent_name VARCHAR(255),
    respondent_email VARCHAR(255),
    respondent_phone VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_form_responses_form_id ON form_responses (form_id);

CREATE TABLE form_answers (
    id BIGSERIAL PRIMARY KEY,
    response_id BIGINT NOT NULL REFERENCES form_responses (id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES form_questions (id) ON DELETE CASCADE,
    answer_value TEXT NOT NULL
);

CREATE INDEX idx_form_answers_response_id ON form_answers (response_id);
