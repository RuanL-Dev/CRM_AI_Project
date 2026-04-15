CREATE TABLE contacts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(255),
    company VARCHAR(255),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE deals (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    deal_value NUMERIC(15, 2) NOT NULL,
    stage VARCHAR(32) NOT NULL,
    expected_close_date DATE,
    contact_id BIGINT NOT NULL REFERENCES contacts (id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_deals_contact_id ON deals (contact_id);
CREATE INDEX idx_deals_stage ON deals (stage);

CREATE TABLE activities (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(32) NOT NULL,
    notes VARCHAR(255) NOT NULL,
    due_at TIMESTAMP WITH TIME ZONE,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    contact_id BIGINT NOT NULL REFERENCES contacts (id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_activities_contact_id ON activities (contact_id);
