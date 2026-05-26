package repository;

import model.MenuCategory;
import model.MenuItem;
import org.springframework.stereotype.Repository;
import utils.DatabaseConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Repository
public class MenuRepository {

    /**
     * Listing 2: Browse menu by category.
     */
    public List<MenuItem> getMenuByRestaurant(int restaurantId) {
        String sql = "SELECT mc.category_id, mc.name AS category_name, " +
                     "mi.item_id, mi.name AS item_name, mi.description, mi.price, mi.image_url " +
                     "FROM Menu_Category mc " +
                     "LEFT JOIN Menu_Item mi ON mi.category_id = mc.category_id " +
                     "WHERE mc.restaurant_id = ? " +
                     "ORDER BY mc.category_id, mi.item_id";
        
        List<MenuItem> menu = new ArrayList<>();
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, restaurantId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (rs.getObject("item_id") != null) {
                        MenuItem item = new MenuItem();
                        item.setCategoryId(rs.getInt("category_id"));
                        item.setCategoryName(rs.getString("category_name"));
                        item.setItemId(rs.getInt("item_id"));
                        item.setName(rs.getString("item_name"));
                        item.setDescription(rs.getString("description"));
                        item.setPrice(rs.getBigDecimal("price"));
                        item.setImageUrl(rs.getString("image_url"));
                        item.setRestaurantId(restaurantId);
                        menu.add(item);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return menu;
    }

    public List<MenuCategory> getCategories(int restaurantId) {
        String sql = "SELECT * FROM Menu_Category WHERE restaurant_id = ?";
        List<MenuCategory> categories = new ArrayList<>();
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, restaurantId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MenuCategory mc = new MenuCategory();
                    mc.setCategoryId(rs.getInt("category_id"));
                    mc.setName(rs.getString("name"));
                    mc.setRestaurantId(rs.getInt("restaurant_id"));
                    categories.add(mc);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    // ----------------------------------------------------------------
    // Write operations
    // ----------------------------------------------------------------

    public MenuCategory addCategory(MenuCategory category) {
        String sql = "INSERT INTO Menu_Category (name, restaurant_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, category.getName());
            stmt.setInt(2, category.getRestaurantId());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) category.setCategoryId(rs.getInt(1));
            }
            return category;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteCategory(int categoryId) {
        String sql = "DELETE FROM Menu_Category WHERE category_id = ?";
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public MenuItem addMenuItem(MenuItem item) {
        String sql = "INSERT INTO Menu_Item (name, description, price, image_url, category_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, item.getName());
            stmt.setString(2, item.getDescription());
            stmt.setBigDecimal(3, item.getPrice());
            stmt.setString(4, item.getImageUrl());
            stmt.setInt(5, item.getCategoryId());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) item.setItemId(rs.getInt(1));
            }
            return item;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateMenuItem(MenuItem item) {
        String sql = "UPDATE Menu_Item SET name=?, description=?, price=?, image_url=?, category_id=? WHERE item_id=?";
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, item.getName());
            stmt.setString(2, item.getDescription());
            stmt.setBigDecimal(3, item.getPrice());
            stmt.setString(4, item.getImageUrl());
            stmt.setInt(5, item.getCategoryId());
            stmt.setInt(6, item.getItemId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteMenuItem(int itemId) {
        String sql = "DELETE FROM Menu_Item WHERE item_id = ?";
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
