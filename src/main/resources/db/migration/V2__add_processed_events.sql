-- Creates table for idempotent Kafka event processing
-- Matches JPA entity pl.budziosz.votingsystem.model.domain.ProcessedEvent

CREATE TABLE IF NOT EXISTS processed_events (
    event_id    VARCHAR(64) PRIMARY KEY,
    occurred_at TIMESTAMP NOT NULL DEFAULT NOW()
);
