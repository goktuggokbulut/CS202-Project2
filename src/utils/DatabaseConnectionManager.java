package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Manages the JDBC connection to the MySQL database.
 * Reads connection settings from application.properties.
 */
public class DatabaseConnectionManager {

    private static String url;
    private static String username;
    private static String password;

    static {
        Properties props = new Properties();
        try (InputStream is = DatabaseConnectionManager.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is == null) {
                // Fallback to defaults if file is missing
                url = "jdbc:mysql://localhost:3306/food_order_db";
                username = "root";
                password = "password";
            } else {
                props.load(is);
                url = props.getProperty("db.url");
                username = props.getProperty("db.username");
                password = props.getProperty("db.password");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
