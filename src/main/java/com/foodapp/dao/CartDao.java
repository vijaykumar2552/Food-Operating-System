// Defines the persistence contract for FoodOS cart dao data.
// Service code depends on this interface rather than JDBC details.

package com.foodapp.dao;

import com.foodapp.model.CartItem;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface CartDao {
    List<CartItem> findItems(Connection connection, long customerId) throws SQLException;
    void addItem(Connection connection, long customerId, long itemId, int quantity) throws SQLException;
    void removeItem(Connection connection, long customerId, long itemId) throws SQLException;
    void setQuantity(Connection connection, long customerId, long itemId, int quantity) throws SQLException;
    void clear(Connection connection, long customerId) throws SQLException;
}
