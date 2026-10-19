// Persists FoodOS jdbc address dao data through JDBC.
// Keeps SQL access and row mapping behind the DAO boundary.

package com.foodapp.dao;

import java.sql.*;

public class JdbcAddressDao implements AddressDao {
    @Override public long findOrCreateDefault(Connection connection, long customerId, String addressLine, String city, String postalCode) throws SQLException {
        try (PreparedStatement find = connection.prepareStatement("SELECT address_id FROM addresses WHERE customer_id = ? AND is_default = TRUE ORDER BY address_id LIMIT 1")) {
            find.setLong(1, customerId);
            try (ResultSet result = find.executeQuery()) {
                if (result.next()) {
                    long addressId = result.getLong(1);
                    try (PreparedStatement update = connection.prepareStatement(
                            "UPDATE addresses SET address_line = ?, city = ?, postal_code = ? WHERE address_id = ? AND customer_id = ?")) {
                        update.setString(1, addressLine);
                        update.setString(2, city);
                        update.setString(3, postalCode);
                        update.setLong(4, addressId);
                        update.setLong(5, customerId);
                        update.executeUpdate();
                    }
                    return addressId;
                }
            }
        }
        try (PreparedStatement insert = connection.prepareStatement("INSERT INTO addresses(customer_id, label, address_line, city, postal_code, is_default) VALUES (?, 'Checkout', ?, ?, ?, TRUE)", Statement.RETURN_GENERATED_KEYS)) { insert.setLong(1, customerId); insert.setString(2, addressLine); insert.setString(3, city); insert.setString(4, postalCode); insert.executeUpdate(); try (ResultSet keys = insert.getGeneratedKeys()) { if (!keys.next()) throw new SQLException("Address id was not generated"); return keys.getLong(1); } }
    }
}
