CREATE TABLE IF NOT EXISTS email_outbox (
                                            id BIGSERIAL PRIMARY KEY,
                                            to_email VARCHAR(255) NOT NULL,
                                            subject VARCHAR(255) NOT NULL,
                                            body TEXT,
                                            status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                            attempts INTEGER NOT NULL DEFAULT 0,
                                            created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                                            updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                                            version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_email_outbox_status ON email_outbox(status);
CREATE INDEX idx_email_outbox_created_at ON email_outbox(created_at);