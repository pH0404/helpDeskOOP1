package helpdesk;

import helpdesk.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionTest {

    public static void main(String[] args) {
        try (Connection connection = DatabaseConfig.getConnection()) {

            System.out.println("Подключение к PostgreSQL успешно");
            System.out.println("База данных: " + connection.getCatalog());
            System.out.println("Пользователь: "
                    + connection.getMetaData().getUserName());
            System.out.println("AutoCommit = "
                    + connection.getAutoCommit());

        } catch (SQLException e) {
            System.err.println("Ошибка подключения к PostgreSQL:");
            e.printStackTrace();
        }
    }
}
