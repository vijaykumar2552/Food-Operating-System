// Applies FoodOS menu item service business rules for API and desktop workflows.
// Coordinates validation and persistence through the service layer.

package com.foodapp.service;

import com.foodapp.dao.MenuItemDao;
import com.foodapp.exception.DatabaseOperationException;
import com.foodapp.model.MenuItem;
import com.foodapp.util.Database;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MenuItemService {
    private final Database database;
    private final MenuItemDao dao;
    public MenuItemService(Database database, MenuItemDao dao) { this.database = database; this.dao = dao; }
    public List<MenuItem> findForRestaurant(long restaurantId, String search, String category) throws DatabaseOperationException { if (restaurantId <= 0) throw new DatabaseOperationException("Invalid restaurant.", new IllegalArgumentException()); try (Connection connection = database.openConnection()) { return dao.findByRestaurant(connection, restaurantId, search, category); } catch (SQLException exception) { throw new DatabaseOperationException("Unable to load the menu.", exception); } }
}
