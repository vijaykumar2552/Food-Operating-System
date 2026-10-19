// Starts the FoodOS HTTP API and wires its runtime dependencies.
// Connects configuration, JDBC-backed services, and request handling at startup.

package com.foodapp.api;

import com.foodapp.config.AppConfig;
import com.foodapp.dao.*;
import com.foodapp.service.*;
import com.foodapp.util.Database;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public final class ApiMain {
    private ApiMain() { }

    public static void main(String[] args) throws IOException {
        AppConfig config = AppConfig.load();
        Database database = new Database(config);
        AuthenticationService authentication = new AuthenticationService(database, new JdbcUserDao());
        RestaurantService restaurants = new RestaurantService(database, new JdbcRestaurantDao());
        MenuItemService menu = new MenuItemService(database, new JdbcMenuItemDao());
        CartService cart = new CartService(database, new JdbcCartDao(), new JdbcMenuItemDao());
        OrderService orders = new OrderService(database, new JdbcCartDao(), new JdbcMenuItemDao(),
                new JdbcOrderDao(), new JdbcAddressDao());

        HttpServer server = HttpServer.create(new InetSocketAddress(config.apiPort()), 0);
        server.createContext("/api", new ApiHandler(database, authentication, restaurants, menu, cart, orders,
                new DashboardService(database)));
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("FoodOS API listening on port " + config.apiPort());
    }
}
