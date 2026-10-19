// Provides shared FoodOS database support used by multiple application layers.
// Centralizes reusable behavior instead of duplicating it in callers.

package com.foodapp.util;

import com.foodapp.config.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private final AppConfig config;

    public Database(AppConfig config) { this.config = config; }

    public Connection openConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("MySQL JDBC driver is unavailable.", exception);
        }
        return DriverManager.getConnection(config.databaseUrl(), config.databaseUsername(), config.databasePassword());
    }
}
