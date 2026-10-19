// Defines the persistence contract for FoodOS address dao data.
// Service code depends on this interface rather than JDBC details.

package com.foodapp.dao;

import java.sql.Connection;
import java.sql.SQLException;

public interface AddressDao {
    long findOrCreateDefault(Connection connection, long customerId, String addressLine, String city, String postalCode) throws SQLException;
}
