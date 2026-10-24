\echo '--- Проверка целостности данных ---'

SELECT
    'Заявки без существующего клиента' AS check_name,
    COUNT(*) AS problem_count
FROM tickets t
LEFT JOIN users u ON u.id = t.customer_id
WHERE u.id IS NULL

UNION ALL

SELECT
    'Назначения не специалистам',
    COUNT(*)
FROM tickets t
JOIN users u ON u.id = t.assignee_id
WHERE u.role <> 'SUPPORT_AGENT'

UNION ALL

SELECT
    'Пустые комментарии',
    COUNT(*)
FROM ticket_comments
WHERE TRIM(text) = ''

UNION ALL

SELECT
    'История без существующей заявки',
    COUNT(*)
FROM ticket_status_history h
LEFT JOIN tickets t ON t.id = h.ticket_id
WHERE t.id IS NULL

UNION ALL

SELECT
    'Несовпадение статуса с последней записью истории',
    COUNT(*)
FROM tickets t
JOIN LATERAL (
    SELECT new_status
    FROM ticket_status_history h
    WHERE h.ticket_id = t.id
    ORDER BY changed_at DESC, id DESC
    LIMIT 1
) last_history ON TRUE
WHERE t.status <> last_history.new_status;
