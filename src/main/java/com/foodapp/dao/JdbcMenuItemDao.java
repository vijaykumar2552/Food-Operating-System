// Persists FoodOS jdbc menu item dao data through JDBC.
// Keeps SQL access and row mapping behind the DAO boundary.

package com.foodapp.dao;

import com.foodapp.model.MenuItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcMenuItemDao implements MenuItemDao {
    @Override
    public List<MenuItem> findByRestaurant(Connection connection, long restaurantId, String search, String category) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT mi.item_id, mc.restaurant_id, mc.category_id, mc.name AS category, mi.name, mi.description, mi.price, mi.discount_price, mi.preparation_time_min, mi.is_veg, mi.availability, mi.image_path, COALESCE(i.quantity_available, 0) AS stock FROM menu_items mi JOIN menu_categories mc ON mc.category_id = mi.category_id LEFT JOIN inventory i ON i.item_id = mi.item_id WHERE mc.restaurant_id = ?");
        if (search != null && !search.isBlank()) sql.append(" AND (mi.name LIKE ? OR mi.description LIKE ?)");
        if (category != null && !category.isBlank() && !"All categories".equals(category)) sql.append(" AND mc.name = ?");
        sql.append(" ORDER BY mc.name, mi.name");
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1; statement.setLong(index++, restaurantId);
            if (search != null && !search.isBlank()) { String value = "%" + search.trim() + "%"; statement.setString(index++, value); statement.setString(index++, value); }
            if (category != null && !category.isBlank() && !"All categories".equals(category)) statement.setString(index, category);
            List<MenuItem> items = new ArrayList<>();
            try (ResultSet result = statement.executeQuery()) { while (result.next()) items.add(map(result)); }
            return items;
        }
    }

    @Override public MenuItem findById(Connection connection, long itemId) throws SQLException { String sql = "SELECT mi.item_id, mc.restaurant_id, mc.category_id, mc.name AS category, mi.name, mi.description, mi.price, mi.discount_price, mi.preparation_time_min, mi.is_veg, mi.availability, mi.image_path, COALESCE(i.quantity_available, 0) AS stock FROM menu_items mi JOIN menu_categories mc ON mc.category_id = mi.category_id LEFT JOIN inventory i ON i.item_id = mi.item_id WHERE mi.item_id = ?"; try (PreparedStatement statement = connection.prepareStatement(sql)) { statement.setLong(1, itemId); try (ResultSet result = statement.executeQuery()) { return result.next() ? map(result) : null; } } }
    @Override public MenuItem lockForCheckout(Connection connection, long itemId) throws SQLException { String sql = "SELECT mi.item_id, mc.restaurant_id, mc.category_id, mc.name AS category, mi.name, mi.description, mi.price, mi.discount_price, mi.preparation_time_min, mi.is_veg, mi.availability, mi.image_path, i.quantity_available AS stock FROM menu_items mi JOIN menu_categories mc ON mc.category_id = mi.category_id JOIN restaurants r ON r.restaurant_id = mc.restaurant_id JOIN inventory i ON i.item_id = mi.item_id WHERE mi.item_id = ? AND r.status = 'ACTIVE' FOR UPDATE"; try (PreparedStatement statement = connection.prepareStatement(sql)) { statement.setLong(1, itemId); try (ResultSet result = statement.executeQuery()) { return result.next() ? map(result) : null; } } }
    @Override public void decreaseInventory(Connection connection, long itemId, int quantity) throws SQLException { try (PreparedStatement statement = connection.prepareStatement("UPDATE inventory SET quantity_available = quantity_available - ? WHERE item_id = ? AND quantity_available >= ?")) { statement.setInt(1, quantity); statement.setLong(2, itemId); statement.setInt(3, quantity); if (statement.executeUpdate() != 1) throw new SQLException("Inventory update rejected"); } }

    private MenuItem map(ResultSet result) throws SQLException {
        String availability = result.getString("availability");
        return new MenuItem(result.getLong("item_id"), result.getLong("restaurant_id"), result.getLong("category_id"), result.getString("category"),
                result.getString("name"), result.getString("description"), result.getBigDecimal("price"),
                result.getBigDecimal("discount_price"), result.getInt("preparation_time_min"),
                result.getBoolean("is_veg"), availability, result.getString("image_path"),
                "AVAILABLE".equals(availability), result.getInt("stock"));
    }
}
