// Loads or exposes FoodOS app config configuration.
// Keeps runtime settings separate from HTTP, service, and persistence code.

package com.foodapp.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AppConfig {
    private final Properties properties = new Properties();

    private AppConfig() {
        Path external = Path.of("config.properties");
        try (InputStream input = Files.exists(external) ? Files.newInputStream(external) : null) {
            if (input != null) properties.load(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load database configuration", exception);
        }
    }

    public static AppConfig load() { return new AppConfig(); }

    public String databaseUrl() { return value("db.url", "DB_URL", "jdbc:mysql://localhost:3306/food_ordering?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true"); }
    public String databaseUsername() { return value("db.username", "DB_USERNAME", "foodapp"); }
    public String databasePassword() { return value("db.password", "DB_PASSWORD", ""); }
    public int apiPort() {
        String configured = value("api.port", "API_PORT", "8080");
        try {
            int port = Integer.parseInt(configured);
            if (port < 1 || port > 65535) throw new NumberFormatException();
            return port;
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("API port must be between 1 and 65535");
        }
    }

    private String value(String property, String environment, String fallback) {
        String configured = properties.getProperty(property);
        if (configured != null && !configured.isBlank()) return configured;
        String envValue = System.getenv(environment);
        return envValue == null ? fallback : envValue;
    }
}
