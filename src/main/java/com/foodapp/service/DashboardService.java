// Applies FoodOS dashboard service business rules for API and desktop workflows.
// Coordinates validation and persistence through the service layer.

package com.foodapp.service;

import com.foodapp.exception.DatabaseOperationException;
import com.foodapp.model.AdminDashboardSummary;
import com.foodapp.model.DashboardSummary;
import com.foodapp.model.User;
import com.foodapp.util.Database;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardService {
    private final Database database;

    public DashboardService(Database database) {
        this.database = database;
    }

    public DashboardSummary customerSummary(User user) throws DatabaseOperationException {
        String restaurantSql = "SELECT COUNT(*) FROM restaurants WHERE status = 'ACTIVE'";
        String activeOrderSql = "SELECT COUNT(*) FROM orders WHERE customer_id = ? AND status IN ('PLACED','ACCEPTED','PREPARING','READY','OUT_FOR_DELIVERY')";
        String recentOrderSql = "SELECT COUNT(*) FROM orders WHERE customer_id = ? AND order_date >= CURRENT_DATE - INTERVAL 30 DAY";
        try (Connection connection = database.openConnection()) {
            long restaurants = scalar(connection, restaurantSql, null);
            long activeOrders = scalar(connection, activeOrderSql, user.getId());
            long recentOrders = scalar(connection, recentOrderSql, user.getId());
            return new DashboardSummary(restaurants, activeOrders, recentOrders);
        } catch (SQLException exception) {
            throw new DatabaseOperationException("Unable to load dashboard data.", exception);
        }
    }

    public AdminDashboardSummary adminSummary() throws DatabaseOperationException {
        String popularSql = "SELECT oi.item_id, mi.name, SUM(oi.quantity) AS quantity " +
                "FROM order_items oi JOIN orders o ON o.order_id = oi.order_id " +
                "JOIN menu_items mi ON mi.item_id = oi.item_id " +
                "WHERE o.status NOT IN ('REJECTED','CANCELLED') " +
                "GROUP BY oi.item_id, mi.name ORDER BY quantity DESC, mi.name LIMIT 5";
        try (Connection connection = database.openConnection()) {
            long restaurants = scalar(connection, "SELECT COUNT(*) FROM restaurants", null);
            long menuItems = scalar(connection, "SELECT COUNT(*) FROM menu_items", null);
            long orders = scalar(connection, "SELECT COUNT(*) FROM orders", null);
            BigDecimal revenue = decimal(connection,
                    "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status NOT IN ('REJECTED','CANCELLED')");
            List<AdminDashboardSummary.PopularItem> popularItems = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(popularSql);
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    popularItems.add(new AdminDashboardSummary.PopularItem(
                            result.getLong("item_id"), result.getString("name"), result.getLong("quantity")));
                }
            }
            return new AdminDashboardSummary(restaurants, menuItems, orders, revenue, popularItems);
        } catch (SQLException exception) {
            throw new DatabaseOperationException("Unable to load admin dashboard data.", exception);
        }
    }

    private long scalar(Connection connection, String sql, Long userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (userId != null) statement.setLong(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getLong(1);
            }
        }
    }

    private BigDecimal decimal(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getBigDecimal(1);
        }
    }
}
