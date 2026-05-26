package repository;

import model.Rating;
import org.springframework.stereotype.Repository;
import utils.DatabaseConnectionManager;

import java.sql.*;
import java.time.LocalDateTime;

@Repository
public class RatingRepository {

    /**
     * Listing 6 Step 1: Read the acceptance timestamp.
     */
    public LocalDateTime getAcceptanceTime(int orderId) {
        String sql = "SELECT time_stamp FROM Order_Status_History WHERE order_id = ? AND status = 'Accepted'";
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("time_stamp").toLocalDateTime();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Returns true if a rating already exists for the given order. */
    public boolean hasRated(int orderId) {
        String sql = "SELECT 1 FROM Rating WHERE order_id = ?";
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Listing 6 Step 2: Insert the rating.
     */
    public boolean submitRating(Rating rating) {
        String sql = "INSERT INTO Rating (score, comment, customer_id, order_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, rating.getScore());
            stmt.setString(2, rating.getComment());
            stmt.setString(3, rating.getCustomerId());
            stmt.setInt(4, rating.getOrderId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
