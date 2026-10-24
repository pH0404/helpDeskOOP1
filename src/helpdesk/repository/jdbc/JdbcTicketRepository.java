package helpdesk.repository.jdbc;

import helpdesk.config.DatabaseConfig;
import helpdesk.model.Ticket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import helpdesk.repository.TicketRepository;
import helpdesk.model.TicketPriority;
import helpdesk.model.TicketStatus;
import helpdesk.model.TicketDetails;
import helpdesk.model.StatusHistoryEntry;
import helpdesk.model.AgentTicketStats;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.List;

public class JdbcTicketRepository implements TicketRepository {
    @Override
    public Ticket save(Ticket ticket) {
        String sql = """
            INSERT INTO tickets(
                title, description, status, priority,
                customer_id, assignee_id, category_id
            )
            VALUES (?, ?, ?::ticket_status, ?::ticket_priority, ?, ?, ?)
            RETURNING id, created_at, updated_at
            """;

        if (ticket.getCustomerId() == null) {
            throw new IllegalArgumentException(
                    "У заявки должен быть указан клиент"
            );
        }

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, ticket.getTitle());
            ps.setString(2, ticket.getDescription());
            ps.setString(3, ticket.getStatus().name());
            ps.setString(4, ticket.getPriority().name());
            ps.setLong(5, ticket.getCustomerId());

            if (ticket.getAssigneeId() == null) {
                ps.setNull(6, Types.BIGINT);
            } else {
                ps.setLong(6, ticket.getAssigneeId());
            }

            if (ticket.getCategoryId() == null) {
                ps.setNull(7, Types.BIGINT);
            } else {
                ps.setLong(7, ticket.getCategoryId());
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ticket.setId(rs.getLong("id"));
                    ticket.setCreatedAt(
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    ticket.setUpdatedAt(
                            rs.getTimestamp("updated_at").toLocalDateTime()
                    );
                }
            }

            return ticket;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Не удалось сохранить заявку", e
            );
        }
    }

    @Override
    public Optional<Ticket> findById(long id) {
        String sql = "SELECT * FROM tickets WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapTicket(rs));
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска заявки", e);
        }
    }
    private Ticket mapTicket(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();

        ticket.setId(rs.getLong("id"));
        ticket.setTitle(rs.getString("title"));
        ticket.setDescription(rs.getString("description"));
        ticket.setStatus(
                TicketStatus.valueOf(rs.getString("status"))
        );
        ticket.setPriority(
                TicketPriority.valueOf(rs.getString("priority"))
        );

        ticket.setCustomerId(rs.getLong("customer_id"));

        long assigneeId = rs.getLong("assignee_id");
        ticket.setAssigneeId(rs.wasNull() ? null : assigneeId);

        long categoryId = rs.getLong("category_id");
        ticket.setCategoryId(rs.wasNull() ? null : categoryId);

        ticket.setCreatedAt(
                rs.getTimestamp("created_at").toLocalDateTime()
        );
        ticket.setUpdatedAt(
                rs.getTimestamp("updated_at").toLocalDateTime()
        );

        Timestamp closed = rs.getTimestamp("closed_at");
        ticket.setClosedAt(
                closed == null ? null : closed.toLocalDateTime()
        );

        return ticket;
    }

    @Override
    public List<Ticket> findAll() {
        String sql = "SELECT * FROM tickets ORDER BY id";
        List<Ticket> result = new java.util.ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapTicket(rs));
            }

            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения заявок", e);
        }
    }

    @Override
    public List<TicketDetails> findAllWithDetails() {
        String sql = """
        SELECT
            t.id AS ticket_id,
            t.title,
            t.status,
            t.priority,
            customer.name AS customer_name,
            assignee.name AS assignee_name,
            c.name AS category_name
        FROM tickets t
        JOIN users customer
            ON customer.id = t.customer_id
        LEFT JOIN users assignee
            ON assignee.id = t.assignee_id
        LEFT JOIN categories c
            ON c.id = t.category_id
        ORDER BY t.id
        """;

        List<TicketDetails> result = new java.util.ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TicketDetails details = new TicketDetails(
                        rs.getLong("ticket_id"),
                        rs.getString("title"),
                        TicketStatus.valueOf(rs.getString("status")),
                        TicketPriority.valueOf(rs.getString("priority")),
                        rs.getString("customer_name"),
                        rs.getString("assignee_name"),
                        rs.getString("category_name")
                );

                result.add(details);
            }

            return result;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Не удалось получить подробную информацию о заявках", e
            );
        }
    }

    @Override
    public List<Ticket> findPage(int page, int size) {
        if (page < 1) {
            throw new IllegalArgumentException(
                    "Номер страницы должен быть не меньше 1"
            );
        }

        if (size < 1) {
            throw new IllegalArgumentException(
                    "Размер страницы должен быть не меньше 1"
            );
        }

        long offset = (long) (page - 1) * size;

        String sql = """
        SELECT *
        FROM tickets
        ORDER BY id
        LIMIT ?
        OFFSET ?
        """;

        List<Ticket> result = new java.util.ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, size);
            ps.setLong(2, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapTicket(rs));
                }
            }

            return result;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Не удалось получить страницу заявок", e
            );
        }
    }

    @Override
    public List<Ticket> searchByText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Поисковая строка не может быть пустой"
            );
        }

        String sql = """
        SELECT *
        FROM tickets
        WHERE title ILIKE ?
           OR description ILIKE ?
        ORDER BY id
        """;

        String pattern = "%" + text.trim() + "%";
        List<Ticket> result = new java.util.ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapTicket(rs));
                }
            }

            return result;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Не удалось выполнить поиск заявок", e
            );
        }
    }


    @Override
    public List<AgentTicketStats> findAgentTicketStats() {
        String sql = """
        SELECT
            u.id AS agent_id,
            u.name AS agent_name,
            COUNT(t.id) FILTER (
                WHERE t.status IN ('NEW', 'IN_PROGRESS')
            ) AS active_tickets,
            COUNT(t.id) FILTER (
                WHERE t.status = 'RESOLVED'
            ) AS resolved_tickets,
            COUNT(t.id) FILTER (
                WHERE t.status = 'CLOSED'
            ) AS closed_tickets
        FROM users u
        LEFT JOIN tickets t
            ON t.assignee_id = u.id
        WHERE u.role = 'SUPPORT_AGENT'
        GROUP BY u.id, u.name
        ORDER BY u.name
        """;

        List<AgentTicketStats> result = new java.util.ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(new AgentTicketStats(
                        rs.getLong("agent_id"),
                        rs.getString("agent_name"),
                        rs.getLong("active_tickets"),
                        rs.getLong("resolved_tickets"),
                        rs.getLong("closed_tickets")
                ));
            }

            return result;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Не удалось получить статистику специалистов", e
            );
        }
    }

    @Override
    public List<Ticket> findByStatus(TicketStatus status) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Статус заявки не может быть null"
            );
        }

        String sql = """
            SELECT * FROM tickets
            WHERE status = ?::ticket_status
            ORDER BY created_at DESC
            """;

        List<Ticket> result = new java.util.ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, status.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapTicket(rs));
                }
            }

            return result;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Ошибка поиска заявок по статусу", e
            );
        }
    }

    @Override
    public void update(Ticket ticket) {
        if (ticket.getId() == null) {
            throw new IllegalArgumentException(
                    "Нельзя обновить заявку без ID"
            );
        }

        if (ticket.getCustomerId() == null) {
            throw new IllegalArgumentException(
                    "У заявки должен быть указан клиент"
            );
        }

        String sql = """
            UPDATE tickets
            SET title = ?,
                description = ?,
                status = ?::ticket_status,
                priority = ?::ticket_priority,
                customer_id = ?,
                assignee_id = ?,
                category_id = ?,
                closed_at = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            RETURNING updated_at
            """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, ticket.getTitle());
            ps.setString(2, ticket.getDescription());
            ps.setString(3, ticket.getStatus().name());
            ps.setString(4, ticket.getPriority().name());
            ps.setLong(5, ticket.getCustomerId());

            if (ticket.getAssigneeId() == null) {
                ps.setNull(6, Types.BIGINT);
            } else {
                ps.setLong(6, ticket.getAssigneeId());
            }

            if (ticket.getCategoryId() == null) {
                ps.setNull(7, Types.BIGINT);
            } else {
                ps.setLong(7, ticket.getCategoryId());
            }

            if (ticket.getClosedAt() == null) {
                ps.setNull(8, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(
                        8,
                        java.sql.Timestamp.valueOf(ticket.getClosedAt())
                );
            }

            ps.setLong(9, ticket.getId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ticket.setUpdatedAt(
                            rs.getTimestamp("updated_at").toLocalDateTime()
                    );
                } else {
                    throw new IllegalArgumentException(
                            "Заявка с ID " + ticket.getId() + " не найдена"
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Не удалось обновить заявку", e);
        }
    }

    @Override
    public void assignTicket(long ticketId, long assigneeId) {
        String sql = """
        UPDATE tickets
        SET assignee_id = ?,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ?
          AND EXISTS (
              SELECT 1
              FROM users
              WHERE id = ?
                AND role = 'SUPPORT_AGENT'
          )
        """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, assigneeId);
            ps.setLong(2, ticketId);
            ps.setLong(3, assigneeId);

            int updatedRows = ps.executeUpdate();

            if (updatedRows == 0) {
                throw new IllegalArgumentException(
                        "Заявка не найдена или пользователь "
                                + "не является специалистом"
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Не удалось назначить специалиста на заявку", e
            );
        }
    }

    @Override
    public List<StatusHistoryEntry> findStatusHistoryByTicketId(
            long ticketId
    ) {
        String sql = """
        SELECT old_status, new_status, changed_at
        FROM ticket_status_history
        WHERE ticket_id = ?
        ORDER BY changed_at, id
        """;

        List<StatusHistoryEntry> history = new java.util.ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, ticketId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String oldStatusValue = rs.getString("old_status");

                    TicketStatus oldStatus = oldStatusValue == null
                            ? null
                            : TicketStatus.valueOf(oldStatusValue);

                    TicketStatus newStatus = TicketStatus.valueOf(
                            rs.getString("new_status")
                    );

                    Timestamp changedAt = rs.getTimestamp("changed_at");

                    history.add(new StatusHistoryEntry(
                            oldStatus,
                            newStatus,
                            changedAt.toLocalDateTime()
                    ));
                }
            }

            return history;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Не удалось получить историю статусов заявки", e
            );
        }
    }

    @Override
    public void changeStatus(
            long ticketId,
            TicketStatus newStatus,
            Long changedById
    ) {
        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Новый статус не может быть null"
            );
        }

        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);

            try {
                TicketStatus currentStatus;

                // Блокируем заявку на время транзакции.
                String selectSql = """
                SELECT status
                FROM tickets
                WHERE id = ?
                FOR UPDATE
                """;

                try (PreparedStatement ps =
                             connection.prepareStatement(selectSql)) {
                    ps.setLong(1, ticketId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                    "Заявка с ID " + ticketId + " не найдена"
                            );
                        }

                        currentStatus = TicketStatus.valueOf(
                                rs.getString("status")
                        );
                    }
                }

                validateStatusTransition(currentStatus, newStatus);

                // Обновляем статус заявки.
                String updateSql = """
                UPDATE tickets
                SET status = ?::ticket_status,
                    closed_at = CASE
                        WHEN ? = 'CLOSED' THEN CURRENT_TIMESTAMP
                        ELSE closed_at
                    END,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

                try (PreparedStatement ps =
                             connection.prepareStatement(updateSql)) {
                    ps.setString(1, newStatus.name());
                    ps.setString(2, newStatus.name());
                    ps.setLong(3, ticketId);
                    ps.executeUpdate();
                }

                // Сохраняем историю изменения статуса.
                String historySql = """
                INSERT INTO ticket_status_history (
                    ticket_id,
                    old_status,
                    new_status,
                    changed_by_id
                )
                VALUES (?, ?::ticket_status, ?::ticket_status, ?)
                """;

                try (PreparedStatement ps =
                             connection.prepareStatement(historySql)) {
                    ps.setLong(1, ticketId);
                    ps.setString(2, currentStatus.name());
                    ps.setString(3, newStatus.name());

                    if (changedById == null) {
                        ps.setNull(4, Types.BIGINT);
                    } else {
                        ps.setLong(4, changedById);
                    }

                    ps.executeUpdate();
                }

                connection.commit();

            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                if (e instanceof SQLException) {
                    throw new RuntimeException(
                            "Не удалось изменить статус заявки", e
                    );
                }

                throw (RuntimeException) e;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Ошибка транзакции изменения статуса", e
            );
        }
    }

    private void validateStatusTransition(
            TicketStatus currentStatus,
            TicketStatus newStatus
    ) {
        boolean valid = switch (currentStatus) {
            case NEW ->
                    newStatus == TicketStatus.IN_PROGRESS
                            || newStatus == TicketStatus.CANCELLED;

            case IN_PROGRESS ->
                    newStatus == TicketStatus.RESOLVED
                            || newStatus == TicketStatus.CANCELLED;

            case RESOLVED ->
                    newStatus == TicketStatus.CLOSED;

            case CLOSED, CANCELLED -> false;
        };

        if (!valid) {
            throw new IllegalArgumentException(
                    "Недопустимый переход статуса: "
                            + currentStatus + " -> " + newStatus
            );
        }
    }


    @Override
    public boolean deleteById(long id) {
        String sql = "DELETE FROM tickets WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Не удалось удалить заявку с ID " + id, e
            );
        }
    }
}