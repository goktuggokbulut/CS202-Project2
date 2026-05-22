package repository;

import model.Coupon;
import utils.DatabaseConnectionManager;

import java.sql.*;

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
}
