package repository;

import model.Restaurant;
import utils.DatabaseConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RestaurantRepository {

    /**
     * Listing 1: Restaurant search with keyword and city filter.
     */
    public List<Restaurant> searchByCityAndKeyword(String city, String keyword) {
        String sql = "SELECT r.restaurant_id, r.name, r.cuisine_type, r.address, r.city, r.manager_id, " +
                     "COUNT(DISTINCT rk.keyword) AS keyword_matches, " +
                     "CASE WHEN COUNT(rt.rating_id) >= 10 THEN AVG(rt.score) ELSE 0 END AS avg_rating, " +
                     "COUNT(rt.rating_id) AS rating_count, " +
                     "CASE WHEN COUNT(rt.rating_id) < 10 THEN 'New' ELSE NULL END AS label " +
                     "FROM Restaurant r " +
                     "LEFT JOIN Restaurant_Keyword rk ON rk.restaurant_id = r.restaurant_id AND rk.keyword LIKE ? " +
                     "LEFT JOIN `Order` o ON o.restaurant_id = r.restaurant_id " +
                     "LEFT JOIN Rating rt ON rt.order_id = o.order_id " +
                     "WHERE r.city = ? " +
                     "GROUP BY r.restaurant_id, r.name, r.cuisine_type, r.address, r.city, r.manager_id " +
                     "ORDER BY keyword_matches DESC, avg_rating DESC";

        List<Restaurant> results = new ArrayList<>();
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, city);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Restaurant rest = mapResultSetToRestaurant(rs);
                    // Additional columns from the complex query
                    rest.setAvgRating(rs.getDouble("avg_rating"));
                    rest.setRatingCount(rs.getInt("rating_count"));
                    rest.setLabel(rs.getString("label"));
                    results.add(rest);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    private Restaurant mapResultSetToRestaurant(ResultSet rs) throws SQLException {
        Restaurant rest = new Restaurant();
        rest.setRestaurantId(rs.getInt("restaurant_id"));
        rest.setName(rs.getString("name"));
        rest.setCuisineType(rs.getString("cuisine_type"));
        rest.setAddress(rs.getString("address"));
        rest.setCity(rs.getString("city"));
        rest.setManagerId(rs.getString("manager_id"));
        return rest;
    }

    /**
     * Listing 7: Monthly statistics — total revenue and order count.
     */
    public void fillGeneralStats(int restaurantId, model.RestaurantStatistics stats) {
        String sql = "SELECT COUNT(*) AS total_orders, COALESCE(SUM(total_amount), 0) AS total_revenue " +
                     "FROM `Order` WHERE restaurant_id = ? AND created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH)";
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, restaurantId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.setTotalOrders(rs.getInt("total_orders"));
                    stats.setTotalRevenue(rs.getBigDecimal("total_revenue"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Listing 8: Monthly statistics — item-wise quantity sold and revenue.
     */
    public List<model.RestaurantStatistics.ItemSale> getMonthlyItemSales(int restaurantId) {
        String sql = "SELECT mi.item_id, mi.name, SUM(oi.quantity) AS units_sold, SUM(oi.quantity * oi.unit_price) AS item_revenue " +
                     "FROM Order_Item oi JOIN `Order` o ON o.order_id = oi.order_id JOIN Menu_Item mi ON mi.item_id = oi.item_id " +
                     "WHERE o.restaurant_id = ? AND o.created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH) " +
                     "GROUP BY mi.item_id, mi.name ORDER BY item_revenue DESC";
        List<model.RestaurantStatistics.ItemSale> sales = new ArrayList<>();
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, restaurantId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    model.RestaurantStatistics.ItemSale sale = new model.RestaurantStatistics.ItemSale();
                    sale.itemId = rs.getInt("item_id");
                    sale.itemName = rs.getString("name");
                    sale.unitsSold = rs.getInt("units_sold");
                    sale.revenue = rs.getBigDecimal("item_revenue");
                    sales.add(sale);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }

    /**
     * Listing 9: Monthly statistics — top customer and highest order.
     */
    public void fillTopStats(int restaurantId, model.RestaurantStatistics stats) {
        // Top Customer by count
        String sql1 = "SELECT o.customer_id, COUNT(*) AS order_count FROM `Order` o " +
                      "WHERE o.restaurant_id = ? AND o.created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH) " +
                      "GROUP BY o.customer_id ORDER BY order_count DESC LIMIT 1";
        // Highest single order value
        String sql2 = "SELECT o.order_id, o.customer_id, o.total_amount, o.created_at FROM `Order` o " +
                      "WHERE o.restaurant_id = ? AND o.created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH) " +
                      "ORDER BY o.total_amount DESC LIMIT 1";

        try (Connection conn = DatabaseConnectionManager.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                stmt.setInt(1, restaurantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        model.RestaurantStatistics.TopCustomer tc = new model.RestaurantStatistics.TopCustomer();
                        tc.customerId = rs.getString("customer_id");
                        tc.orderCount = rs.getInt("order_count");
                        stats.setTopCustomerByCount(tc);
                    }
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                stmt.setInt(1, restaurantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        model.RestaurantStatistics.TopOrder to = new model.RestaurantStatistics.TopOrder();
                        to.orderId = rs.getInt("order_id");
                        to.customerId = rs.getString("customer_id");
                        to.amount = rs.getBigDecimal("total_amount");
                        to.timestamp = rs.getTimestamp("created_at").toLocalDateTime();
                        stats.setTopOrder(to);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Listing 10: Monthly statistics — most-ordered item, top category, total coupon discount.
     */
    public void fillAggregatedStats(int restaurantId, model.RestaurantStatistics stats) {
        // Most ordered item
        String sql1 = "SELECT mi.name, SUM(oi.quantity) AS units_sold FROM Order_Item oi " +
                      "JOIN `Order` o ON o.order_id = oi.order_id JOIN Menu_Item mi ON mi.item_id = oi.item_id " +
                      "WHERE o.restaurant_id = ? AND o.created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH) " +
                      "GROUP BY mi.item_id, mi.name ORDER BY units_sold DESC LIMIT 1";
        // Category with highest revenue
        String sql2 = "SELECT mc.name, SUM(oi.quantity * oi.unit_price) AS category_revenue " +
                      "FROM Order_Item oi JOIN Menu_Item mi ON mi.item_id = oi.item_id " +
                      "JOIN Menu_Category mc ON mc.category_id = mi.category_id JOIN `Order` o ON o.order_id = oi.order_id " +
                      "WHERE o.restaurant_id = ? AND o.created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH) " +
                      "GROUP BY mc.category_id, mc.name ORDER BY category_revenue DESC LIMIT 1";
        // Total discount
        String sql3 = "SELECT COALESCE(SUM(discount_applied), 0) AS total_coupon_discount FROM `Order` " +
                      "WHERE restaurant_id = ? AND coupon_id IS NOT NULL AND created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH)";

        try (Connection conn = DatabaseConnectionManager.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                stmt.setInt(1, restaurantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) stats.setMostOrderedItem(rs.getString("name"));
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                stmt.setInt(1, restaurantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) stats.setTopRevenueCategory(rs.getString("name"));
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(sql3)) {
                stmt.setInt(1, restaurantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) stats.setTotalCouponDiscount(rs.getBigDecimal("total_coupon_discount"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
