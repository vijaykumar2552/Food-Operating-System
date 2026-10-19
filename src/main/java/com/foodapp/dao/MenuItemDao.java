// Defines the persistence contract for FoodOS menu item dao data.
// Service code depends on this interface rather than JDBC details.

package com.foodapp.dao;

import com.foodapp.model.MenuItem;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface MenuItemDao {
    List<MenuItem> findByRestaurant(Connection connection, long restaurantId, String search, String category) throws SQLException;
    MenuItem findById(Connection connection, long itemId) throws SQLException;
    MenuItem lockForCheckout(Connection connection, long itemId) throws SQLException;
    void decreaseInventory(Connection connection, long itemId, int quantity) throws SQLException;
}
