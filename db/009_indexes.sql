CREATE INDEX idx_tickets_status
    ON tickets(status);

CREATE INDEX idx_tickets_assignee
    ON tickets(assignee_id);

CREATE INDEX idx_tickets_customer
    ON tickets(customer_id);

CREATE INDEX idx_tickets_category
    ON tickets(category_id);

CREATE INDEX idx_comments_ticket
    ON ticket_comments(ticket_id);

CREATE INDEX idx_status_history_ticket
    ON ticket_status_history(ticket_id);
