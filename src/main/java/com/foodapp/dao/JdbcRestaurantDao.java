// Persists FoodOS jdbc restaurant dao data through JDBC.
// Keeps SQL access and row mapping behind the DAO boundary.

package com.foodapp.dao;

import com.foodapp.model.Restaurant;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcRestaurantDao implements RestaurantDao {
    private static final String BASE = "SELECT restaurant_id, owner_user_id, name, description, cuisine, location, " +
            "contact_phone, opening_hours, rating, delivery_time_minutes, delivery_distance_km, delivery_charge, " +
            "status, cover_image_path, map_address, map_lat, map_lng, map_link " +
            "FROM restaurants WHERE status IN ('ACTIVE', 'CLOSED')";

    @Override
    public List<Restaurant> findActive(Connection connection, String search, String cuisine) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE);
        if (search != null && !search.isBlank()) sql.append(" AND (name LIKE ? OR location LIKE ?)");
        if (cuisine != null && !cuisine.isBlank() && !"All cuisines".equals(cuisine)) sql.append(" AND cuisine = ?");
        sql.append(" ORDER BY rating DESC, name");
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            if (search != null && !search.isBlank()) { String value = "%" + search.trim() + "%"; statement.setString(index++, value); statement.setString(index++, value); }
            if (cuisine != null && !cuisine.isBlank() && !"All cuisines".equals(cuisine)) statement.setString(index, cuisine);
            return readList(statement);
        }
    }

    @Override
    public Restaurant findActiveById(Connection connection, long restaurantId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(BASE + " AND restaurant_id = ?")) {
            statement.setLong(1, restaurantId);
            try (ResultSet result = statement.executeQuery()) { return result.next() ? map(result) : null; }
        }
    }

    private List<Restaurant> readList(PreparedStatement statement) throws SQLException {
        List<Restaurant> restaurants = new ArrayList<>();
        try (ResultSet result = statement.executeQuery()) { while (result.next()) restaurants.add(map(result)); }
        return restaurants;
    }

    private Restaurant map(ResultSet result) throws SQLException {
        return new Restaurant(
                result.getLong("restaurant_id"),
                result.getLong("owner_user_id"),
                result.getString("name"),
                result.getString("description"),
                result.getString("cuisine"),
                result.getString("location"),
                result.getString("contact_phone"),
                result.getString("opening_hours"),
                result.getBigDecimal("rating"),
                result.getInt("delivery_time_minutes"),
                result.getBigDecimal("delivery_distance_km"),
                result.getBigDecimal("delivery_charge"),
                result.getString("status"),
                result.getString("cover_image_path"),
                result.getString("map_address"),
                result.getBigDecimal("map_lat"),
                result.getBigDecimal("map_lng"),
                result.getString("map_link")
        );
    }
}
