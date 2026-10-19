// Defines the persistence contract for FoodOS restaurant dao data.
// Service code depends on this interface rather than JDBC details.

package com.foodapp.dao;

import com.foodapp.model.Restaurant;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface RestaurantDao {
    List<Restaurant> findActive(Connection connection, String search, String cuisine) throws SQLException;
    Restaurant findActiveById(Connection connection, long restaurantId) throws SQLException;
}
