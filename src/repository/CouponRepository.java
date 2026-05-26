package repository;

import model.Coupon;
import org.springframework.stereotype.Repository;
import utils.DatabaseConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CouponRepository {

    /**
     * Listing 3 Step 1: Validate coupon.
     */
    public Coupon getValidCoupon(int couponId, int restaurantId) {
        String sql = "SELECT * FROM Coupon WHERE coupon_id = ? AND restaurant_id = ? " +
                     "AND is_active = TRUE AND CURRENT_TIMESTAMP BETWEEN valid_from AND valid_until";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, couponId);
            stmt.setInt(2, restaurantId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Coupon coupon = new Coupon();
                    coupon.setCouponId(rs.getInt("coupon_id"));
                    coupon.setCode(rs.getString("code"));
                    coupon.setDiscountType(rs.getString("discount_type"));
                    coupon.setDiscountValue(rs.getBigDecimal("discount_value"));
                    coupon.setRestaurantId(rs.getInt("restaurant_id"));
                    return coupon;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Finds a valid coupon by its code and restaurantId.
     */
    public Coupon getValidCouponByCode(String code, int restaurantId) {
        String sql = "SELECT * FROM Coupon WHERE code = ? AND restaurant_id = ? " +
                     "AND is_active = TRUE AND CURRENT_TIMESTAMP BETWEEN valid_from AND valid_until";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, code);
            stmt.setInt(2, restaurantId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Coupon coupon = new Coupon();
                    coupon.setCouponId(rs.getInt("coupon_id"));
                    coupon.setCode(rs.getString("code"));
                    coupon.setDiscountType(rs.getString("discount_type"));
                    coupon.setDiscountValue(rs.getBigDecimal("discount_value"));
                    coupon.setRestaurantId(rs.getInt("restaurant_id"));
                    return coupon;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ----------------------------------------------------------------
    // Write / list operations
    // ----------------------------------------------------------------

    public List<Coupon> getCouponsByRestaurant(int restaurantId) {
        String sql = "SELECT * FROM Coupon WHERE restaurant_id = ? ORDER BY valid_until DESC";
        List<Coupon> coupons = new ArrayList<>();
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, restaurantId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    coupons.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return coupons;
    }

    public Coupon createCoupon(Coupon coupon) {
        String sql = "INSERT INTO Coupon (code, discount_type, discount_value, valid_from, valid_until, is_active, restaurant_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, coupon.getCode());
            stmt.setString(2, coupon.getDiscountType());
            stmt.setBigDecimal(3, coupon.getDiscountValue());
            stmt.setDate(4, java.sql.Date.valueOf(coupon.getValidFrom()));
            stmt.setDate(5, java.sql.Date.valueOf(coupon.getValidUntil()));
            stmt.setBoolean(6, coupon.isActive());
            stmt.setInt(7, coupon.getRestaurantId());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) coupon.setCouponId(rs.getInt(1));
            }
            return coupon;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deactivateCoupon(int couponId) {
        String sql = "UPDATE Coupon SET is_active = FALSE WHERE coupon_id = ?";
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, couponId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Coupon mapRow(ResultSet rs) throws SQLException {
        Coupon c = new Coupon();
        c.setCouponId(rs.getInt("coupon_id"));
        c.setCode(rs.getString("code"));
        c.setDiscountType(rs.getString("discount_type"));
        c.setDiscountValue(rs.getBigDecimal("discount_value"));
        c.setValidFrom(rs.getDate("valid_from").toLocalDate());
        c.setValidUntil(rs.getDate("valid_until").toLocalDate());
        c.setActive(rs.getBoolean("is_active"));
        c.setRestaurantId(rs.getInt("restaurant_id"));
        return c;
    }
}
