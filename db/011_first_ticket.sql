INSERT INTO tickets(
    title,
    description,
    status,
    priority,
    customer_id,
    category_id
)
VALUES (
    'Не работает Wi-Fi',
    'Ноутбук подключается к сети, но Интернет недоступен',
    'NEW',
    'HIGH',
    1,
    1
)
RETURNING id, title, status, created_at;
