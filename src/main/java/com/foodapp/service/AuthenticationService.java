// Applies FoodOS authentication service business rules for API and desktop workflows.
// Coordinates validation and persistence through the service layer.

package com.foodapp.service;

import com.foodapp.dao.UserDao;
import com.foodapp.exception.AppException;
import com.foodapp.exception.AuthenticationException;
import com.foodapp.model.User;
import com.foodapp.util.Database;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class AuthenticationService {
    private final Database database;
    private final UserDao userDao;

    public AuthenticationService(Database database, UserDao userDao) {
        this.database = database;
        this.userDao = userDao;
    }

    public User login(String email, String password) throws AppException {
        validateEmail(email);
        if (password == null || password.isBlank()) throw new AuthenticationException("Enter your password.");
        try (Connection connection = database.openConnection()) {
            Optional<User> user = userDao.findByEmail(connection, email.trim().toLowerCase());
            if (user.isEmpty() || !BCrypt.checkpw(password, passwordHash(connection, email))) {
                throw new AuthenticationException("The email or password is incorrect.");
            }
            if (!user.get().isActive()) throw new AuthenticationException("This account is inactive.");
            userDao.recordAudit(connection, user.get().getId(), "LOGIN", "USER", user.get().getId());
            return user.get();
        } catch (SQLException exception) {
            throw new com.foodapp.exception.DatabaseOperationException("Unable to connect to the database.", exception);
        }
    }

    public void register(String fullName, String email, String password, String phone) throws AppException {
        if (fullName == null || fullName.isBlank()) throw new AuthenticationException("Enter your full name.");
        validateEmail(email);
        if (password == null || password.length() < 8) throw new AuthenticationException("Password must contain at least 8 characters.");
        try (Connection connection = database.openConnection()) {
            connection.setAutoCommit(false);
            try {
                if (userDao.findByEmail(connection, email.trim().toLowerCase()).isPresent()) {
                    throw new AuthenticationException("An account with this email already exists.");
                }
                long userId = userDao.createCustomer(connection, email.trim().toLowerCase(), BCrypt.hashpw(password, BCrypt.gensalt(12)), fullName.trim());
                userDao.createCustomerProfile(connection, userId, phone == null ? "" : phone.trim());
                userDao.recordAudit(connection, userId, "REGISTER", "USER", userId);
                connection.commit();
            } catch (AppException | SQLException exception) {
                connection.rollback();
                if (exception instanceof AppException appException) throw appException;
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new com.foodapp.exception.DatabaseOperationException("Registration could not be completed.", exception);
        }
    }

    private String passwordHash(Connection connection, String email) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT password_hash FROM users WHERE email = ?")) {
            statement.setString(1, email.trim().toLowerCase());
            try (var result = statement.executeQuery()) {
                return result.next() ? result.getString(1) : "invalid";
            }
        }
    }

    private void validateEmail(String email) throws AuthenticationException {
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) throw new AuthenticationException("Enter a valid email address.");
    }
}
