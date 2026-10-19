// Persists FoodOS jdbc cart dao data through JDBC.
// Keeps SQL access and row mapping behind the DAO boundary.

package com.foodapp.dao;

import com.foodapp.model.CartItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcCartDao implements CartDao {
    private static final String ITEM_QUERY = "SELECT ci.item_id, mc.restaurant_id, r.name AS restaurant_name, mi.name AS item_name, mi.price, ci.quantity, mi.availability FROM carts c JOIN cart_items ci ON ci.cart_id = c.cart_id JOIN menu_items mi ON mi.item_id = ci.item_id JOIN menu_categories mc ON mc.category_id = mi.category_id JOIN restaurants r ON r.restaurant_id = mc.restaurant_id WHERE c.customer_id = ? ORDER BY mi.name";
    @Override public List<CartItem> findItems(Connection connection, long customerId) throws SQLException { try (PreparedStatement statement = connection.prepareStatement(ITEM_QUERY)) { statement.setLong(1, customerId); List<CartItem> items = new ArrayList<>(); try (ResultSet result = statement.executeQuery()) { while (result.next()) items.add(new CartItem(result.getLong("item_id"), result.getLong("restaurant_id"), result.getString("restaurant_name"), result.getString("item_name"), result.getBigDecimal("price"), result.getInt("quantity"), "AVAILABLE".equals(result.getString("availability")))); } return items; } }
    @Override public void addItem(Connection connection, long customerId, long itemId, int quantity) throws SQLException { long cartId = ensureCart(connection, customerId); String sql = "INSERT INTO cart_items(cart_id, item_id, quantity) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)"; try (PreparedStatement statement = connection.prepareStatement(sql)) { statement.setLong(1, cartId); statement.setLong(2, itemId); statement.setInt(3, quantity); statement.executeUpdate(); } }
    @Override public void removeItem(Connection connection, long customerId, long itemId) throws SQLException { try (PreparedStatement statement = connection.prepareStatement("DELETE ci FROM cart_items ci JOIN carts c ON c.cart_id = ci.cart_id WHERE c.customer_id = ? AND ci.item_id = ?")) { statement.setLong(1, customerId); statement.setLong(2, itemId); statement.executeUpdate(); } }
    @Override public void setQuantity(Connection connection, long customerId, long itemId, int quantity) throws SQLException { try (PreparedStatement statement = connection.prepareStatement("UPDATE cart_items ci JOIN carts c ON c.cart_id = ci.cart_id SET ci.quantity = ? WHERE c.customer_id = ? AND ci.item_id = ?")) { statement.setInt(1, quantity); statement.setLong(2, customerId); statement.setLong(3, itemId); if (statement.executeUpdate() != 1) throw new SQLException("Cart item was not updated"); } }
    @Override public void clear(Connection connection, long customerId) throws SQLException { try (PreparedStatement statement = connection.prepareStatement("DELETE ci FROM cart_items ci JOIN carts c ON c.cart_id = ci.cart_id WHERE c.customer_id = ?")) { statement.setLong(1, customerId); statement.executeUpdate(); } }
    private long ensureCart(Connection connection, long customerId) throws SQLException { try (PreparedStatement statement = connection.prepareStatement("INSERT INTO carts(customer_id) VALUES (?) ON DUPLICATE KEY UPDATE cart_id = LAST_INSERT_ID(cart_id)", Statement.RETURN_GENERATED_KEYS)) { statement.setLong(1, customerId); statement.executeUpdate(); try (ResultSet keys = statement.getGeneratedKeys()) { if (!keys.next()) throw new SQLException("Cart id was not generated"); return keys.getLong(1); } } }
}
