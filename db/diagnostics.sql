\echo '--- Подключение к базе ---'

SELECT
    current_database() AS database_name,
    current_user AS database_user,
    CURRENT_TIMESTAMP AS checked_at;


\echo '--- Количество записей в основных таблицах ---'

SELECT 'users' AS table_name, COUNT(*) AS row_count
FROM users
UNION ALL
SELECT 'tickets', COUNT(*) FROM tickets
UNION ALL
SELECT 'ticket_comments', COUNT(*) FROM ticket_comments
UNION ALL
SELECT 'ticket_status_history', COUNT(*) FROM ticket_status_history
UNION ALL
SELECT 'categories', COUNT(*) FROM categories
UNION ALL
SELECT 'departments', COUNT(*) FROM departments
ORDER BY table_name;


\echo '--- Сводка по заявкам ---'

SELECT
    status,
    COUNT(*) AS total,
    COUNT(*) FILTER (
        WHERE assignee_id IS NOT NULL
    ) AS assigned
FROM tickets
GROUP BY status
ORDER BY status;


\echo '--- Проверка дат закрытия ---'

SELECT
    COUNT(*) FILTER (
        WHERE status = 'CLOSED'
          AND closed_at IS NULL
    ) AS closed_without_date,
    COUNT(*) FILTER (
        WHERE status <> 'CLOSED'
          AND closed_at IS NOT NULL
    ) AS not_closed_with_date
FROM tickets;


\echo '--- Последние 10 изменений статусов ---'

SELECT
    h.ticket_id,
    h.old_status,
    h.new_status,
    u.name AS changed_by,
    h.changed_at
FROM ticket_status_history h
LEFT JOIN users u ON u.id = h.changed_by_id
ORDER BY h.changed_at DESC, h.id DESC
LIMIT 10;
