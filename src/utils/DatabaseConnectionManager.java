package utils;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Spring-managed component that exposes a static getConnection() method.
 * Spring injects the auto-configured HikariCP DataSource at startup,
 * so all repository classes continue to call DatabaseConnectionManager.getConnection()
 * without any changes to their SQL code.
 */
@Component
public class DatabaseConnectionManager {

    private static DataSource dataSource;

    public DatabaseConnectionManager(DataSource dataSource) {
        DatabaseConnectionManager.dataSource = dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
