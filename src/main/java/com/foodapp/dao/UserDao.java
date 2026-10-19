// Defines the persistence contract for FoodOS user dao data.
// Service code depends on this interface rather than JDBC details.

package com.foodapp.dao;

import com.foodapp.model.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public interface UserDao {
    Optional<User> findByEmail(Connection connection, String email) throws SQLException;
    long createCustomer(Connection connection, String email, String passwordHash, String fullName) throws SQLException;
    void createCustomerProfile(Connection connection, long userId, String phone) throws SQLException;
    void recordAudit(Connection connection, long userId, String action, String entityType, Long entityId) throws SQLException;
}
