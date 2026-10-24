package helpdesk.repository.jdbc;

import helpdesk.config.DatabaseConfig;
import helpdesk.model.TicketComment;
import helpdesk.repository.TicketCommentRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class JdbcTicketCommentRepository
        implements TicketCommentRepository {

    @Override
    public TicketComment save(TicketComment comment) {
        String sql = """
            INSERT INTO ticket_comments (
                ticket_id,
                author_id,
                text,
                internal
            )
            VALUES (?, ?, ?, ?)
            RETURNING id, created_at
            """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, comment.getTicketId());
            ps.setLong(2, comment.getAuthorId());
            ps.setString(3, comment.getText());
            ps.setBoolean(4, comment.isInternal());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    comment.setId(rs.getLong("id"));

                    Timestamp createdAt = rs.getTimestamp("created_at");
                    comment.setCreatedAt(createdAt.toLocalDateTime());
                }
            }

            return comment;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Не удалось сохранить комментарий", e
            );
        }
    }

    @Override
    public List<TicketComment> findByTicketId(long ticketId) {
        String sql = """
            SELECT id, ticket_id, author_id, text, internal, created_at
            FROM ticket_comments
            WHERE ticket_id = ?
            ORDER BY created_at, id
            """;

        List<TicketComment> comments = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, ticketId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TicketComment comment = new TicketComment(
                            rs.getLong("ticket_id"),
                            rs.getLong("author_id"),
                            rs.getString("text"),
                            rs.getBoolean("internal")
                    );

                    comment.setId(rs.getLong("id"));

                    Timestamp createdAt = rs.getTimestamp("created_at");
                    comment.setCreatedAt(createdAt.toLocalDateTime());

                    comments.add(comment);
                }
            }

            return comments;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Не удалось получить комментарии заявки", e
            );
        }
    }

    @Override
    public List<TicketComment> findExternalByTicketId(long ticketId) {
        String sql = """
        SELECT id, ticket_id, author_id, text, internal, created_at
        FROM ticket_comments
        WHERE ticket_id = ?
          AND internal = FALSE
        ORDER BY created_at, id
        """;

        List<TicketComment> comments = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, ticketId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TicketComment comment = new TicketComment(
                            rs.getLong("ticket_id"),
                            rs.getLong("author_id"),
                            rs.getString("text"),
                            rs.getBoolean("internal")
                    );

                    comment.setId(rs.getLong("id"));
                    comment.setCreatedAt(
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );

                    comments.add(comment);
                }
            }

            return comments;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Не удалось получить внешние комментарии заявки", e
            );
        }
    }
}