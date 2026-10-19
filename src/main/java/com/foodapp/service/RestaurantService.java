// Applies FoodOS restaurant service business rules for API and desktop workflows.
// Coordinates validation and persistence through the service layer.

package com.foodapp.service;

import com.foodapp.dao.RestaurantDao;
import com.foodapp.exception.DatabaseOperationException;
import com.foodapp.exception.RestaurantNotFoundException;
import com.foodapp.model.Restaurant;
import com.foodapp.util.Database;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class RestaurantService {
    private final Database database;
    private final RestaurantDao dao;
    public RestaurantService(Database database, RestaurantDao dao) { this.database = database; this.dao = dao; }
    public List<Restaurant> search(String query, String cuisine) throws DatabaseOperationException { try (Connection connection = database.openConnection()) { return dao.findActive(connection, query, cuisine); } catch (SQLException exception) { throw new DatabaseOperationException("Unable to load restaurants.", exception); } }
    public Restaurant getActive(long id) throws DatabaseOperationException, RestaurantNotFoundException { if (id <= 0) throw new RestaurantNotFoundException("Restaurant was not found."); try (Connection connection = database.openConnection()) { Restaurant restaurant = dao.findActiveById(connection, id); if (restaurant == null) throw new RestaurantNotFoundException("Restaurant is unavailable."); return restaurant; } catch (RestaurantNotFoundException exception) { throw exception; } catch (SQLException exception) { throw new DatabaseOperationException("Unable to load restaurant details.", exception); } }
}
