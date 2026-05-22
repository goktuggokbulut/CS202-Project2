package repository;

import model.MenuCategory;
import model.MenuItem;
import utils.DatabaseConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
}
