CREATE TABLE ticket_status_history (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL
        REFERENCES tickets(id) ON DELETE CASCADE,
    old_status ticket_status,
    new_status ticket_status NOT NULL,
    changed_by_id BIGINT REFERENCES users(id),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
