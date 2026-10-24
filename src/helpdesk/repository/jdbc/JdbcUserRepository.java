package helpdesk.repository.jdbc;

import helpdesk.config.DatabaseConfig;
import helpdesk.model.Administrator;
import helpdesk.model.Customer;
import helpdesk.model.SupportAgent;
import helpdesk.model.User;
import helpdesk.model.UserRole;
import helpdesk.repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {

    @Override
    public Optional<User> findById(long id) {
        String sql = """
                SELECT id, name, email, role
                FROM users
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Не удалось найти пользователя с ID " + id, e);
        }
    }


    @Override
    public Optional<User> findByEmail(String email) {
        String sql = """
            SELECT id, name, email, role
            FROM users
            WHERE email = ?
            """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Не удалось найти пользователя по email " + email, e
            );
        }
    }
    @Override
    public List<User> findAll() {
        String sql = """
                SELECT id, name, email, role
                FROM users
                ORDER BY id
                """;

        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }

            return users;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Не удалось получить список пользователей", e);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        UserRole role = UserRole.valueOf(rs.getString("role"));

        return switch (role) {
            case CUSTOMER -> new Customer(id, name, email);
            case SUPPORT_AGENT -> new SupportAgent(id, name, email);
            case ADMIN -> new Administrator(id, name, email);
        };
    }
}