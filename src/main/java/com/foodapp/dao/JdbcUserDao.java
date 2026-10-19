// Persists FoodOS jdbc user dao data through JDBC.
// Keeps SQL access and row mapping behind the DAO boundary.

package com.foodapp.dao;

import com.foodapp.model.Role;
import com.foodapp.model.User;
import java.sql.*;
import java.util.Optional;

public class JdbcUserDao implements UserDao {
    @Override
    public Optional<User> findByEmail(Connection connection, String email) throws SQLException {
        String sql = "SELECT user_id, email, full_name, role, is_active FROM users WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return Optional.empty();
                return Optional.of(new User(result.getLong("user_id"), result.getString("email"),
                        result.getString("full_name"), Role.valueOf(result.getString("role")),
                        result.getBoolean("is_active")));
            }
        }
    }

    @Override
    public long createCustomer(Connection connection, String email, String passwordHash, String fullName) throws SQLException {
        String sql = "INSERT INTO users(email, password_hash, full_name, role) VALUES (?, ?, ?, 'CUSTOMER')";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, email);
            statement.setString(2, passwordHash);
            statement.setString(3, fullName);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("User id was not generated");
                return keys.getLong(1);
            }
        }
    }

    @Override
    public void createCustomerProfile(Connection connection, long userId, String phone) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO customers(customer_id, phone) VALUES (?, ?)")) {
            statement.setLong(1, userId);
            statement.setString(2, phone);
            statement.executeUpdate();
        }
    }

    @Override
    public void recordAudit(Connection connection, long userId, String action, String entityType, Long entityId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO audit_logs(user_id, action, entity_type, entity_id) VALUES (?, ?, ?, ?)")) {
            statement.setLong(1, userId);
            statement.setString(2, action);
            statement.setString(3, entityType);
            if (entityId == null) statement.setNull(4, Types.BIGINT); else statement.setLong(4, entityId);
            statement.executeUpdate();
        }
    }
}
