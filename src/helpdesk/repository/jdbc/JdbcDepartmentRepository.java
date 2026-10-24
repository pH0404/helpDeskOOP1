package helpdesk.repository.jdbc;

import helpdesk.config.DatabaseConfig;
import helpdesk.model.Department;
import helpdesk.repository.DepartmentRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcDepartmentRepository implements DepartmentRepository {

    @Override
    public Optional<Department> findById(long id) {
        String sql = """
                SELECT id, name, description, created_at
                FROM departments
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapDepartment(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Не удалось найти отдел с ID " + id, e);
        }
    }

    @Override
    public List<Department> findAll() {
        String sql = """
                SELECT id, name, description, created_at
                FROM departments
                ORDER BY id
                """;

        List<Department> departments = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                departments.add(mapDepartment(rs));
            }

            return departments;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Не удалось получить список отделов", e);
        }
    }

    private Department mapDepartment(ResultSet rs) throws SQLException {
        return new Department(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}