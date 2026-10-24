\echo '--- Количество заявок по статусам ---'

SELECT status, COUNT(*) AS ticket_count
FROM tickets
GROUP BY status
ORDER BY status;


\echo '--- Количество заявок по приоритетам ---'

SELECT priority, COUNT(*) AS ticket_count
FROM tickets
GROUP BY priority
ORDER BY priority;


\echo '--- Нагрузка специалистов ---'

SELECT
    u.id AS agent_id,
    u.name AS agent_name,
    COUNT(t.id) AS total_tickets,
    COUNT(t.id) FILTER (
        WHERE t.status IN ('NEW', 'IN_PROGRESS')
    ) AS active_tickets,
    COUNT(t.id) FILTER (
        WHERE t.status = 'CLOSED'
    ) AS closed_tickets
FROM users u
LEFT JOIN tickets t ON t.assignee_id = u.id
WHERE u.role = 'SUPPORT_AGENT'
GROUP BY u.id, u.name
ORDER BY total_tickets DESC, u.name;


\echo '--- Заявки по категориям ---'

SELECT
    COALESCE(c.name, 'Без категории') AS category,
    COUNT(t.id) AS ticket_count
FROM tickets t
LEFT JOIN categories c ON c.id = t.category_id
GROUP BY c.name
ORDER BY ticket_count DESC;


\echo '--- Среднее время решения закрытых заявок ---'

SELECT
    ROUND(
        AVG(EXTRACT(EPOCH FROM (closed_at - created_at)) / 3600)::numeric,
        2
    ) AS average_resolution_hours
FROM tickets
WHERE status = 'CLOSED'
  AND closed_at IS NOT NULL;
