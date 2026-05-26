package repository;

import model.Order;
import model.OrderItem;
import model.OrderStatusHistory;
import org.springframework.stereotype.Repository;
import utils.DatabaseConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderRepository {

    /**
     * Listing 3: Place an order.
     * Executed as a single transaction.
     */
    public boolean placeOrder(Order order, List<OrderItem> items) {
        String sqlOrder = "INSERT INTO `Order` (customer_id, restaurant_id, coupon_id, status, total_amount, discount_applied) " +
                          "VALUES (?, ?, ?, 'Sent', ?, ?)";
        String sqlItem = "INSERT INTO Order_Item (order_id, item_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        String sqlHistory = "INSERT INTO Order_Status_History (order_id, status) VALUES (?, 'Sent')";

        try (Connection conn = DatabaseConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmtOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement stmtItem = conn.prepareStatement(sqlItem);
                 PreparedStatement stmtHistory = conn.prepareStatement(sqlHistory)) {
                
                // 1. Insert Order
                stmtOrder.setString(1, order.getCustomerId());
                stmtOrder.setInt(2, order.getRestaurantId());
                if (order.getCouponId() != null && order.getCouponId() > 0) stmtOrder.setInt(3, order.getCouponId());
                else stmtOrder.setNull(3, Types.INTEGER);
                stmtOrder.setBigDecimal(4, order.getTotalAmount());
                stmtOrder.setBigDecimal(5, order.getDiscountApplied());
                stmtOrder.executeUpdate();

                // Get generated order_id
                ResultSet rs = stmtOrder.getGeneratedKeys();
                if (!rs.next()) throw new SQLException("Failed to get order_id");
                int orderId = rs.getInt(1);

                // 2. Insert Order Items
                for (OrderItem item : items) {
                    stmtItem.setInt(1, orderId);
                    stmtItem.setInt(2, item.getItemId());
                    stmtItem.setInt(3, item.getQuantity());
                    stmtItem.setBigDecimal(4, item.getUnitPrice());
                    stmtItem.addBatch();
                }
                stmtItem.executeBatch();

                // 3. Log initial status
                stmtHistory.setInt(1, orderId);
                stmtHistory.executeUpdate();

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

    /**
     * Listing 4: Track order status with full history.
     */
    public List<OrderStatusHistory> getStatusHistory(int orderId) {
        String sql = "SELECT * FROM Order_Status_History WHERE order_id = ? ORDER BY time_stamp ASC";
        List<OrderStatusHistory> history = new ArrayList<>();
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderStatusHistory entry = new OrderStatusHistory();
                    entry.setOrderId(rs.getInt("order_id"));
                    entry.setStatus(rs.getString("status"));
                    entry.setTimeStamp(rs.getTimestamp("time_stamp").toLocalDateTime());
                    history.add(entry);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    /**
     * Listing 5: Restaurant accepts an incoming order.
     */
    public boolean acceptOrder(int orderId) {
        String sqlUpdate  = "UPDATE `Order` SET status = 'Accepted' WHERE order_id = ?";
        // Log Preparing then Accepted so all three stages appear in Order_Status_History.
        String sqlPrepare = "INSERT INTO Order_Status_History (order_id, status) VALUES (?, 'Preparing')";
        String sqlAccept  = "INSERT INTO Order_Status_History (order_id, status) VALUES (?, 'Accepted')";

        try (Connection conn = DatabaseConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmtUpd  = conn.prepareStatement(sqlUpdate);
                 PreparedStatement stmtPrep = conn.prepareStatement(sqlPrepare);
                 PreparedStatement stmtAcc  = conn.prepareStatement(sqlAccept)) {

                stmtUpd.setInt(1, orderId);
                stmtUpd.executeUpdate();

                stmtPrep.setInt(1, orderId);
                stmtPrep.executeUpdate();

                stmtAcc.setInt(1, orderId);
                stmtAcc.executeUpdate();

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

    public List<Order> getOrdersByCustomer(String customerId) {
        String sql = "SELECT o.*, r.name as restaurant_name FROM `Order` o JOIN Restaurant r ON o.restaurant_id = r.restaurant_id WHERE customer_id = ? ORDER BY created_at DESC";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order o = mapResultSetToOrder(rs);
                    o.setRestaurantName(rs.getString("restaurant_name"));
                    orders.add(o);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setCustomerId(rs.getString("customer_id"));
        o.setRestaurantId(rs.getInt("restaurant_id"));
        o.setCouponId(rs.getInt("coupon_id"));
        o.setStatus(rs.getString("status"));
        o.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setDiscountApplied(rs.getBigDecimal("discount_applied"));
        return o;
    }

    public List<Order> getOrdersByRestaurantAndStatus(int restaurantId, String status) {
        String sql = "SELECT o.*, u.email as customer_email FROM `Order` o " +
                     "JOIN User u ON o.customer_id = u.username " +
                     "WHERE o.restaurant_id = ? AND o.status = ? ORDER BY o.created_at ASC";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, restaurantId);
            stmt.setString(2, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order o = mapResultSetToOrder(rs);
                    // Using restaurantName field for customer email in this view context
                    o.setRestaurantName(rs.getString("customer_email")); 
                    orders.add(o);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<OrderItem> getOrderItems(int orderId) {
        String sql = "SELECT oi.*, mi.name FROM Order_Item oi " +
                     "JOIN Menu_Item mi ON oi.item_id = mi.item_id WHERE oi.order_id = ?";
        List<OrderItem> items = new ArrayList<>();
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setItemId(rs.getInt("item_id"));
                    item.setItemName(rs.getString("name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
}
