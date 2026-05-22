package repository;

import model.User;
import utils.DatabaseConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    public boolean register(User user, String type, List<String> addresses, List<String> phones) {
        String sqlUser = "INSERT INTO User (username, password, email, city) VALUES (?, ?, ?, ?)";
        String sqlSubtype = type.equalsIgnoreCase("Customer") ? 
                            "INSERT INTO Customer (username) VALUES (?)" : 
                            "INSERT INTO Restaurant_Manager (username) VALUES (?)";
        String sqlAddress = "INSERT INTO User_Address (username, address) VALUES (?, ?)";
        String sqlPhone = "INSERT INTO User_Phone (username, phone_number) VALUES (?, ?)";

        try (Connection conn = DatabaseConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmtUser = conn.prepareStatement(sqlUser);
                 PreparedStatement stmtSub = conn.prepareStatement(sqlSubtype);
                 PreparedStatement stmtAddr = conn.prepareStatement(sqlAddress);
                 PreparedStatement stmtPhone = conn.prepareStatement(sqlPhone)) {
                
                // 1. Insert User
                stmtUser.setString(1, user.getUsername());
                stmtUser.setString(2, user.getPassword());
                stmtUser.setString(3, user.getEmail());
                stmtUser.setString(4, user.getCity());
                stmtUser.executeUpdate();

                // 2. Insert Subtype
                stmtSub.setString(1, user.getUsername());
                stmtSub.executeUpdate();

                // 3. Insert Addresses
                for (String addr : addresses) {
                    stmtAddr.setString(1, user.getUsername());
                    stmtAddr.setString(2, addr);
                    stmtAddr.addBatch();
                }
                stmtAddr.executeBatch();

                // 4. Insert Phones
                for (String phone : phones) {
                    stmtPhone.setString(1, user.getUsername());
                    stmtPhone.setString(2, phone);
                    stmtPhone.addBatch();
                }
                stmtPhone.executeBatch();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public User login(String username, String password) {
        String sql = "SELECT u.*, " +
                     "CASE WHEN c.username IS NOT NULL THEN 'Customer' " +
                     "     WHEN rm.username IS NOT NULL THEN 'RestaurantManager' " +
                     "     ELSE NULL END AS user_type " +
                     "FROM User u " +
                     "LEFT JOIN Customer c ON u.username = c.username " +
                     "LEFT JOIN Restaurant_Manager rm ON u.username = rm.username " +
                     "WHERE u.username = ? AND u.password = ?";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setEmail(rs.getString("email"));
                    user.setCity(rs.getString("city"));
                    user.setUserType(rs.getString("user_type"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getAddresses(String username) {
        String sql = "SELECT address FROM User_Address WHERE username = ?";
        List<String> addresses = new ArrayList<>();
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    addresses.add(rs.getString("address"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return addresses;
    }

    public List<String> getPhones(String username) {
        String sql = "SELECT phone_number FROM User_Phone WHERE username = ?";
        List<String> phones = new ArrayList<>();
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    phones.add(rs.getString("phone_number"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return phones;
    }
}
