
CREATE TYPE user_role AS ENUM (
    'CUSTOMER',
    'SUPPORT_AGENT',
    'ADMIN'
);

CREATE TYPE ticket_status AS ENUM (
    'NEW',
    'IN_PROGRESS',
    'RESOLVED',
    'CLOSED',
    'CANCELLED'
);

CREATE TYPE ticket_priority AS ENUM (
    'LOW',
    'MEDIUM',
    'HIGH',
    'CRITICAL'
);
