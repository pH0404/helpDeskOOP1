package helpdesk.repository.jdbc;

import helpdesk.config.DatabaseConfig;
import helpdesk.model.Category;
import helpdesk.model.TicketPriority;
import helpdesk.repository.CategoryRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCategoryRepository implements CategoryRepository {

    @Override
    public Optional<Category> findById(long id) {
        String sql = """
                SELECT id, name, description, default_priority, active
                FROM categories
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapCategory(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Не удалось найти категорию с ID " + id, e);
        }
    }

    @Override
    public List<Category> findAll() {
        String sql = """
                SELECT id, name, description, default_priority, active
                FROM categories
                ORDER BY id
                """;

        List<Category> categories = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                categories.add(mapCategory(rs));
            }

            return categories;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Не удалось получить список категорий", e);
        }
    }

    private Category mapCategory(ResultSet rs) throws SQLException {
        return new Category(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                TicketPriority.valueOf(rs.getString("default_priority")),
                rs.getBoolean("active")
        );
    }
}