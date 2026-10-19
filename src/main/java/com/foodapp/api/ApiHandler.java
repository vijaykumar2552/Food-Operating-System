// Maps FoodOS HTTP requests to application services and response envelopes.
// Applies request validation and authorization at the API boundary.

package com.foodapp.api;

import com.foodapp.dao.*;
import com.foodapp.exception.AppException;
import com.foodapp.exception.AuthenticationException;
import com.foodapp.exception.DatabaseOperationException;
import com.foodapp.exception.InvalidOrderException;
import com.foodapp.exception.OrderNotFoundException;
import com.foodapp.exception.RestaurantNotFoundException;
import com.foodapp.model.*;
import com.foodapp.service.*;
import com.foodapp.util.Database;
import com.google.gson.*;
import com.sun.net.httpserver.*;
import java.io.*;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

final class ApiHandler implements HttpHandler {
    private static final Gson GSON = new Gson();
    private final Database database;
    private final AuthenticationService authentication;
    private final RestaurantService restaurants;
    private final MenuItemService menu;
    private final CartService cart;
    private final OrderService orders;
    private final DashboardService dashboard;
    private final SessionStore sessions = new SessionStore();

    ApiHandler(Database database, AuthenticationService authentication, RestaurantService restaurants,
               MenuItemService menu, CartService cart, OrderService orders, DashboardService dashboard) {
        this.database = database;
        this.authentication = authentication;
        this.restaurants = restaurants;
        this.menu = menu;
        this.cart = cart;
        this.orders = orders;
        this.dashboard = dashboard;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            addCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 204, null);
                return;
            }
            dispatch(exchange);
        } catch (BadRequest exception) {
            sendError(exchange, 400, exception.getMessage());
        } catch (Unauthorized exception) {
            sendError(exchange, 401, exception.getMessage());
        } catch (Forbidden exception) {
            sendError(exchange, 403, exception.getMessage());
        } catch (NotFound exception) {
            sendError(exchange, 404, exception.getMessage());
        } catch (Conflict exception) {
            sendError(exchange, 409, exception.getMessage());
        } catch (AuthenticationException exception) {
            sendError(exchange, 401, exception.getMessage());
        } catch (InvalidOrderException exception) {
            sendError(exchange, 400, exception.getMessage());
        } catch (DatabaseOperationException exception) {
            sendError(exchange, 503, exception.getMessage());
        } catch (OrderNotFoundException | RestaurantNotFoundException exception) {
            sendError(exchange, 404, exception.getMessage());
        } catch (Exception exception) {
            System.err.println("FoodOS API request failed: " + exception.getClass().getName()
                    + ": " + exception.getMessage());
            exception.printStackTrace(System.err);
            sendError(exchange, 500, "The server could not complete the request.");
        }
    }

    private void dispatch(HttpExchange exchange) throws Exception {
        String methodName = exchange.getRequestMethod().toUpperCase(Locale.ROOT);
        String[] parts = path(exchange.getRequestURI());
        if (parts.length == 1 && "health".equals(parts[0]) && "GET".equals(methodName)) {
            send(exchange, 200, object("status", "ok"));
            return;
        }
        if (parts.length == 2 && "auth".equals(parts[0])) {
            if ("POST".equals(methodName) && "register".equals(parts[1])) register(exchange);
            else if ("POST".equals(methodName) && "login".equals(parts[1])) login(exchange);
            else if ("POST".equals(methodName) && "logout".equals(parts[1])) logout(exchange);
            else throw new NotFound();
            return;
        }
        if (parts.length >= 1 && "restaurants".equals(parts[0])) {
            publicRestaurants(exchange, methodName, parts);
            return;
        }
        if (parts.length == 2 && "orders".equals(parts[0]) && "my".equals(parts[1])
                && "GET".equals(methodName)) {
            User user = requireCustomer(exchange);
            List<OrderSummary> result = orders.history(user.getId());
            JsonArray data = new JsonArray();
            result.forEach(order -> data.add(orderSummary(order)));
            send(exchange, 200, data);
            return;
        }
        if (parts.length == 1 && "orders".equals(parts[0]) && "POST".equals(methodName)) {
            User user = requireCustomer(exchange);
            JsonObject body = body(exchange);
            String payment = string(body, "paymentMethod", "CASH");
            if (!"CASH".equalsIgnoreCase(payment)) throw new BadRequest("Only Cash on Delivery is supported.");
            List<OrderService.CheckoutLine> lines = checkoutLines(body);
            long id = lines == null
                    ? orders.checkout(user.getId(), required(body, "addressLine"), required(body, "city"), required(body, "postalCode"), PaymentMethod.CASH)
                    : orders.checkout(user.getId(), lines, required(body, "addressLine"), required(body, "city"), required(body, "postalCode"), PaymentMethod.CASH);
            send(exchange, 201, object("orderId", id));
            return;
        }
        if (parts.length >= 1 && "cart".equals(parts[0])) {
            User user = requireCustomer(exchange);
            if (parts.length == 1 && "GET".equals(methodName)) {
                send(exchange, 200, cartJson(cart.getCart(user.getId())));
            } else if (parts.length == 1 && "DELETE".equals(methodName)) {
                cart.clear(user.getId());
                send(exchange, 204, null);
            } else if (parts.length == 2 && "items".equals(parts[1]) && "POST".equals(methodName)) {
                JsonObject body = body(exchange);
                cart.add(user.getId(), number(body, "itemId"), quantity(body));
                send(exchange, 204, null);
            } else if (parts.length == 3 && "items".equals(parts[1]) && "PUT".equals(methodName)) {
                JsonObject body = body(exchange);
                cart.changeQuantity(user.getId(), id(parts[2]), quantity(body));
                send(exchange, 204, null);
            } else {
                throw new NotFound();
            }
            return;
        }
        if (parts.length >= 1 && "admin".equals(parts[0])) {
            requireAdmin(exchange);
            admin(exchange, methodName, parts);
            return;
        }
        throw new NotFound();
    }

    private static int quantity(JsonObject body) {
        try {
            int value = body.get("quantity").getAsInt();
            if (value < 1 || value > 50) throw new BadRequest("Quantity must be between 1 and 50.");
            return value;
        } catch (BadRequest exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BadRequest("Quantity must be between 1 and 50.");
        }
    }

    private static List<OrderService.CheckoutLine> checkoutLines(JsonObject body) {
        if (!body.has("items")) return null;
        if (!body.get("items").isJsonArray() || body.getAsJsonArray("items").isEmpty()) throw new BadRequest("Checkout must contain at least one item.");
        List<OrderService.CheckoutLine> lines = new ArrayList<>();
        Set<Long> ids = new HashSet<>();
        for (JsonElement element : body.getAsJsonArray("items")) {
            if (!element.isJsonObject()) throw new BadRequest("Each checkout item must be an object.");
            JsonObject item = element.getAsJsonObject();
            long itemId = number(item, "itemId");
            int itemQuantity = quantity(item);
            if (!ids.add(itemId)) throw new BadRequest("A checkout item was submitted more than once.");
            lines.add(new OrderService.CheckoutLine(itemId, itemQuantity));
        }
        return lines;
    }

    private void register(HttpExchange exchange) throws Exception {
        JsonObject body = body(exchange);
        authentication.register(required(body, "fullName"), required(body, "email"),
                required(body, "password"), string(body, "phone", ""));
        send(exchange, 201, object("message", "Registration successful."));
    }

    private void login(HttpExchange exchange) throws Exception {
        JsonObject body = body(exchange);
        User user = authentication.login(required(body, "email"), required(body, "password"));
        String token = sessions.create(user);
        JsonObject data = userJson(user);
        data.addProperty("token", token);
        send(exchange, 200, data);
    }

    private void logout(HttpExchange exchange) {
        sessions.revoke(token(exchange));
        send(exchange, 204, null);
    }

    private void publicRestaurants(HttpExchange exchange, String methodName, String[] parts) throws Exception {
        if (!"GET".equals(methodName)) throw new NotFound();
        if (parts.length == 1) {
            Map<String, String> query = query(exchange.getRequestURI());
            send(exchange, 200, restaurantsJson(restaurants.search(query.get("search"), query.get("cuisine"))));
        } else if (parts.length == 2) {
            send(exchange, 200, restaurantJson(restaurants.getActive(id(parts[1]))));
        } else if (parts.length == 3 && "menu".equals(parts[2])) {
            Map<String, String> query = query(exchange.getRequestURI());
            send(exchange, 200, menuJson(menu.findForRestaurant(id(parts[1]), query.get("search"), query.get("category"))));
        } else throw new NotFound();
    }

    private void admin(HttpExchange exchange, String methodName, String[] parts) throws Exception {
        if (parts.length >= 2 && "restaurants".equals(parts[1])) {
            if (parts.length == 2 && "GET".equals(methodName)) send(exchange, 200, adminRestaurants());
            else if (parts.length == 2 && "POST".equals(methodName)) createRestaurant(exchange);
            else if (parts.length == 3 && "PUT".equals(methodName)) updateRestaurant(exchange, id(parts[2]));
            else if (parts.length == 3 && "DELETE".equals(methodName)) delete(exchange, "restaurants", id(parts[2]));
            else throw new NotFound();
            return;
        }
        if (parts.length == 2 && "categories".equals(parts[1]) && "GET".equals(methodName)) {
            send(exchange, 200, allCategories());
            return;
        }
        if (parts.length >= 2 && "menu-items".equals(parts[1])) {
            if (parts.length == 2 && "GET".equals(methodName)) {
                Map<String, String> filters = query(exchange.getRequestURI());
                Long restaurantId = filters.containsKey("restaurantId") ? id(filters.get("restaurantId")) : null;
                send(exchange, 200, allMenuItems(restaurantId));
            }
            else if (parts.length == 2 && "POST".equals(methodName)) createMenuItem(exchange);
            else if (parts.length == 3 && "PUT".equals(methodName)) updateMenuItem(exchange, id(parts[2]));
            else if (parts.length == 3 && "DELETE".equals(methodName)) delete(exchange, "menu_items", id(parts[2]));
            else throw new NotFound();
            return;
        }
        if (parts.length == 2 && "orders".equals(parts[1])) {
            if ("GET".equals(methodName)) send(exchange, 200, allOrders());
            else throw new NotFound();
            return;
        }
        if (parts.length == 2 && "dashboard".equals(parts[1]) && "GET".equals(methodName)) {
            requireAdmin(exchange);
            AdminDashboardSummary summary = dashboard.adminSummary();
            JsonObject data = new JsonObject();
            data.addProperty("restaurantCount", summary.restaurantCount());
            data.addProperty("menuItemCount", summary.menuItemCount());
            data.addProperty("orderCount", summary.orderCount());
            data.addProperty("revenue", summary.revenue());
            JsonArray popularItems = new JsonArray();
            summary.popularItems().forEach(item -> {
                JsonObject row = new JsonObject();
                row.addProperty("itemId", item.itemId());
                row.addProperty("name", item.name());
                row.addProperty("quantity", item.quantity());
                popularItems.add(row);
            });
            data.add("popularItems", popularItems);
            send(exchange, 200, data);
            return;
        }
        if (parts.length == 3 && "orders".equals(parts[1]) && "PUT".equals(methodName)) {
            updateOrderStatus(exchange, id(parts[2]));
            return;
        }
        throw new NotFound();
    }

    private JsonArray adminRestaurants() throws SQLException {
        JsonArray result = new JsonArray();
        try (Connection c = database.openConnection();
             PreparedStatement s = c.prepareStatement("SELECT restaurant_id, owner_user_id, name, description, cuisine, location, contact_phone, opening_hours, rating, delivery_time_minutes, delivery_distance_km, delivery_charge, status, cover_image_path, map_address, map_lat, map_lng, map_link FROM restaurants ORDER BY name");
             ResultSet r = s.executeQuery()) {
            while (r.next()) result.add(restaurantRow(r));
        }
        return result;
    }

    private void createRestaurant(HttpExchange exchange) throws Exception {
        JsonObject b = body(exchange);
        String name = required(b, "name"), cuisine = required(b, "cuisine"), location = required(b, "location");
        User admin = requireAdmin(exchange);
        String sql = "INSERT INTO restaurants(owner_user_id, name, description, cuisine, location, contact_phone, opening_hours, delivery_time_minutes, delivery_charge, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = database.openConnection(); PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setLong(1, admin.getId()); s.setString(2, name); s.setString(3, string(b, "description", null));
            s.setString(4, cuisine); s.setString(5, location); s.setString(6, string(b, "contactPhone", null));
            s.setString(7, string(b, "openingHours", null));
            s.setInt(8, integer(b, "deliveryTimeMinutes", 30));
            BigDecimal deliveryCharge = decimal(b, "deliveryCharge", false);
            s.setBigDecimal(9, deliveryCharge == null ? BigDecimal.ZERO : deliveryCharge);
            s.setString(10, status(b, "PENDING"));
            s.executeUpdate();
            try (ResultSet keys = s.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Restaurant id was not generated");
                send(exchange, 201, object("restaurantId", keys.getLong(1)));
            }
        }
    }

    private void updateRestaurant(HttpExchange exchange, long id) throws Exception {
        JsonObject b = body(exchange);
        String sql = "UPDATE restaurants SET name = COALESCE(?, name), description = COALESCE(?, description), cuisine = COALESCE(?, cuisine), location = COALESCE(?, location), delivery_time_minutes = COALESCE(?, delivery_time_minutes), delivery_charge = COALESCE(?, delivery_charge), status = COALESCE(?, status) WHERE restaurant_id = ?";
        try (Connection c = database.openConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            setNullable(s, 1, b, "name"); setNullable(s, 2, b, "description"); setNullable(s, 3, b, "cuisine");
            setNullable(s, 4, b, "location");
            if (b.has("deliveryTimeMinutes")) s.setInt(5, integer(b, "deliveryTimeMinutes", 30)); else s.setNull(5, Types.INTEGER);
            BigDecimal deliveryCharge = decimal(b, "deliveryCharge", false);
            if (deliveryCharge == null) s.setNull(6, Types.DECIMAL); else s.setBigDecimal(6, deliveryCharge);
            s.setString(7, b.has("status") ? status(b, null) : null); s.setLong(8, id);
            if (s.executeUpdate() == 0) throw new NotFound();
        }
        send(exchange, 200, object("message", "Restaurant updated."));
    }

    private JsonArray allMenuItems(Long restaurantId) throws SQLException {
        JsonArray result = new JsonArray();
        String sql = "SELECT mi.item_id, mc.restaurant_id, mc.category_id, mc.name AS category, mi.name, mi.description, mi.price, mi.discount_price, mi.preparation_time_min, mi.is_veg, mi.availability, mi.image_path, COALESCE(i.quantity_available, 0) AS stock FROM menu_items mi JOIN menu_categories mc ON mc.category_id = mi.category_id LEFT JOIN inventory i ON i.item_id = mi.item_id" +
                (restaurantId == null ? "" : " WHERE mc.restaurant_id = ?") +
                " ORDER BY mc.restaurant_id, mc.name, mi.name";
        try (Connection c = database.openConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            if (restaurantId != null) s.setLong(1, restaurantId);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) result.add(menuRow(r));
            }
        }
        return result;
    }

    private JsonArray allCategories() throws SQLException {
            JsonArray result = new JsonArray();
            try (Connection c = database.openConnection();
                 PreparedStatement s = c.prepareStatement("SELECT category_id, restaurant_id, name, sort_order FROM menu_categories ORDER BY restaurant_id, sort_order, name");
                 ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    JsonObject row = new JsonObject();
                    row.addProperty("id", r.getLong(1)); row.addProperty("restaurantId", r.getLong(2));
                    row.addProperty("name", r.getString(3)); row.addProperty("sortOrder", r.getInt(4));
                    result.add(row);
                }
            }
            return result;
        }

    private void createMenuItem(HttpExchange exchange) throws Exception {
        JsonObject b = body(exchange);
        long categoryId = number(b, "categoryId");
        String availability = availability(b, "AVAILABLE");
        String sql = "INSERT INTO menu_items(category_id, name, description, price, discount_price, preparation_time_min, is_veg, availability) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = database.openConnection(); PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            c.setAutoCommit(false);
            try {
                validateCategory(c, categoryId);
                s.setLong(1, categoryId); s.setString(2, required(b, "name")); s.setString(3, string(b, "description", null));
                s.setBigDecimal(4, decimal(b, "price", true)); setNullableDecimal(s, 5, b, "discountPrice");
                s.setInt(6, integer(b, "preparationTimeMinutes", 15)); s.setBoolean(7, bool(b, "isVeg", true)); s.setString(8, availability);
                s.executeUpdate();
                try (ResultSet keys = s.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Menu item id was not generated");
                    long itemId = keys.getLong(1);
                    try (PreparedStatement inventory = c.prepareStatement("INSERT INTO inventory(item_id, quantity_available, reorder_level) VALUES (?, 0, 5)")) {
                        inventory.setLong(1, itemId);
                        inventory.executeUpdate();
                    }
                    c.commit();
                    send(exchange, 201, object("itemId", itemId));
                }
            } catch (Exception exception) {
                c.rollback();
                throw exception;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private void updateMenuItem(HttpExchange exchange, long id) throws Exception {
        JsonObject b = body(exchange);
        List<String> sets = new ArrayList<>(); List<Object> values = new ArrayList<>();
        optionalSet(b, "name", "name", sets, values); optionalSet(b, "description", "description", sets, values);
        if (b.has("price")) { sets.add("price = ?"); values.add(decimal(b, "price", true)); }
        if (b.has("categoryId")) {
            long categoryId = number(b, "categoryId");
            sets.add("category_id = ?"); values.add(categoryId);
        }
        if (b.has("discountPrice")) { sets.add("discount_price = ?"); values.add(decimal(b, "discountPrice", false)); }
        if (b.has("availability")) { sets.add("availability = ?"); values.add(availability(b, null)); }
        if (b.has("isVeg")) { sets.add("is_veg = ?"); values.add(bool(b, "isVeg", true)); }
        if (b.has("preparationTimeMinutes")) { sets.add("preparation_time_min = ?"); values.add(integer(b, "preparationTimeMinutes", 15)); }
        if (sets.isEmpty()) throw new BadRequest("No menu item fields were provided.");
        try (Connection c = database.openConnection()) {
            if (b.has("categoryId")) validateCategoryForItem(c, id, number(b, "categoryId"));
            try (PreparedStatement s = c.prepareStatement("UPDATE menu_items SET " + String.join(", ", sets) + " WHERE item_id = ?")) {
            int index = 1; for (Object value : values) s.setObject(index++, value); s.setLong(index, id);
            if (s.executeUpdate() == 0) throw new NotFound();
            }
        }
        send(exchange, 200, object("message", "Menu item updated."));
    }

    private void validateCategory(Connection connection, long categoryId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT category_id FROM menu_categories WHERE category_id = ?")) {
            statement.setLong(1, categoryId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new BadRequest("Category does not exist.");
            }
        }
    }

    private void validateCategoryForItem(Connection connection, long itemId, long categoryId) throws SQLException {
        String sql = "SELECT mc.restaurant_id FROM menu_categories mc " +
                "WHERE mc.category_id = ? AND EXISTS (" +
                "SELECT 1 FROM menu_items mi JOIN menu_categories current_category " +
                "ON current_category.category_id = mi.category_id " +
                "WHERE mi.item_id = ? AND current_category.restaurant_id = mc.restaurant_id)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, categoryId);
            statement.setLong(2, itemId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new BadRequest("Category must belong to the item's restaurant.");
            }
        }
    }

    private JsonArray allOrders() throws SQLException {
        JsonArray result = new JsonArray();
        String sql = "SELECT o.order_id, o.customer_id, r.name, o.order_date, o.status, o.total_amount FROM orders o JOIN restaurants r ON r.restaurant_id = o.restaurant_id ORDER BY o.order_date DESC";
        try (Connection c = database.openConnection(); PreparedStatement s = c.prepareStatement(sql); ResultSet r = s.executeQuery()) {
            while (r.next()) {
                JsonObject row = new JsonObject(); row.addProperty("id", r.getLong(1)); row.addProperty("customerId", r.getLong(2));
                row.addProperty("restaurantName", r.getString(3)); row.addProperty("orderDate", r.getTimestamp(4).toLocalDateTime().toString());
                row.addProperty("status", r.getString(5)); row.addProperty("totalAmount", r.getBigDecimal(6)); result.add(row);
            }
        }
        return result;
    }

    private void updateOrderStatus(HttpExchange exchange, long id) throws Exception {
        JsonObject b = body(exchange); String status = required(b, "status");
        try { OrderStatus.valueOf(status); } catch (IllegalArgumentException exception) { throw new BadRequest("Unknown order status."); }
        try (Connection c = database.openConnection();
             PreparedStatement current = c.prepareStatement("SELECT status FROM orders WHERE order_id = ?");
             PreparedStatement update = c.prepareStatement("UPDATE orders SET status = ? WHERE order_id = ?")) {
            current.setLong(1, id);
            String currentStatus;
            try (ResultSet result = current.executeQuery()) {
                if (!result.next()) throw new NotFound();
                currentStatus = result.getString(1);
            }
            if (!validTransition(currentStatus, status)) throw new BadRequest("This order status transition is not allowed.");
            update.setString(1, status); update.setLong(2, id); update.executeUpdate();
        }
        send(exchange, 200, object("message", "Order status updated."));
    }

    private static boolean validTransition(String current, String next) {
        if (current.equals(next)) return true;
        return switch (current) {
            case "PLACED" -> Set.of("ACCEPTED", "REJECTED", "CANCELLED").contains(next);
            case "ACCEPTED" -> Set.of("PREPARING", "CANCELLED").contains(next);
            case "PREPARING" -> Set.of("READY", "CANCELLED").contains(next);
            case "READY" -> "OUT_FOR_DELIVERY".equals(next);
            case "OUT_FOR_DELIVERY" -> "DELIVERED".equals(next);
            default -> false;
        };
    }

    private void delete(HttpExchange exchange, String table, long id) throws Exception {
        String sql = "DELETE FROM " + table + " WHERE " + ("restaurants".equals(table) ? "restaurant_id" : "item_id") + " = ?";
        try (Connection c = database.openConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, id); if (s.executeUpdate() == 0) throw new NotFound();
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new Conflict("Cannot delete a record that is still in use.");
        }
        send(exchange, 204, null);
    }

    private User requireCustomer(HttpExchange exchange) throws Unauthorized {
        User user = authenticated(exchange);
        if (user.getRole() != Role.CUSTOMER) throw new Forbidden("Customer access is required.");
        return user;
    }

    private User requireAdmin(HttpExchange exchange) throws Unauthorized, Forbidden {
        User user = authenticated(exchange);
        if (user.getRole() != Role.ADMIN) throw new Forbidden("Admin access is required.");
        return user;
    }

    private User authenticated(HttpExchange exchange) throws Unauthorized {
        User user = sessions.find(token(exchange));
        if (user == null) throw new Unauthorized("Authentication is required.");
        return user;
    }

    private static String token(HttpExchange exchange) {
        String header = exchange.getRequestHeaders().getFirst("Authorization");
        return header != null && header.startsWith("Bearer ") ? header.substring(7).trim() : null;
    }

    private JsonObject body(HttpExchange exchange) throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            String text = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            JsonElement parsed = JsonParser.parseString(text);
            if (!parsed.isJsonObject()) throw new BadRequest("Request body must be a JSON object.");
            return parsed.getAsJsonObject();
        } catch (JsonParseException | IllegalStateException exception) {
            throw new BadRequest("Request body contains invalid JSON.");
        }
    }

    private static String required(JsonObject body, String name) {
        String value = string(body, name, null);
        if (value == null || value.isBlank()) throw new BadRequest("Missing required field: " + name);
        return value.trim();
    }

    private static String string(JsonObject body, String name, String fallback) {
        JsonElement value = body.get(name);
        return value == null || value.isJsonNull() ? fallback : value.isJsonPrimitive() ? value.getAsString() : fallback;
    }

    private static long number(JsonObject body, String name) {
        try { long value = body.get(name).getAsLong(); if (value <= 0) throw new BadRequest(name + " must be positive."); return value; } catch (BadRequest exception) { throw exception; } catch (Exception exception) { throw new BadRequest("Field must be a number: " + name); }
    }

    private static int integer(JsonObject body, String name, int fallback) {
        if (!body.has(name)) return fallback;
        try { int value = body.get(name).getAsInt(); if (value < 1) throw new BadRequest(name + " must be positive."); return value; }
        catch (BadRequest exception) { throw exception; }
        catch (Exception exception) { throw new BadRequest("Field must be a number: " + name); }
    }

    private static BigDecimal decimal(JsonObject body, String name, boolean required) {
        if (!body.has(name) && !required) return null;
        try { BigDecimal value = body.get(name).getAsBigDecimal(); if (value.signum() < 0 || (required && value.signum() == 0)) throw new BadRequest(name + " must not be negative."); return value; }
        catch (Exception exception) { throw new BadRequest("Field must be a decimal: " + name); }
    }

    private static boolean bool(JsonObject body, String name, boolean fallback) {
        if (!body.has(name)) return fallback;
        try { return body.get(name).getAsBoolean(); } catch (Exception exception) { throw new BadRequest("Field must be boolean: " + name); }
    }

    private static String availability(JsonObject body, String fallback) {
        String value = string(body, "availability", fallback);
        if (value == null || !Set.of("AVAILABLE", "SOLD_OUT", "HIDDEN").contains(value)) throw new BadRequest("Unknown availability.");
        return value;
    }

    private static String status(JsonObject body, String fallback) {
        String value = string(body, "status", fallback);
        if (value == null || !Set.of("PENDING", "ACTIVE", "SUSPENDED", "CLOSED").contains(value)) throw new BadRequest("Unknown restaurant status.");
        return value;
    }

    private static void setNullable(PreparedStatement statement, int index, JsonObject body, String name) throws SQLException {
        if (body.has(name) && !body.get(name).isJsonNull()) statement.setString(index, required(body, name)); else statement.setNull(index, Types.VARCHAR);
    }

    private static void setNullableDecimal(PreparedStatement statement, int index, JsonObject body, String name) throws SQLException {
        BigDecimal value = decimal(body, name, false); if (value == null) statement.setNull(index, Types.DECIMAL); else statement.setBigDecimal(index, value);
    }

    private static void optionalSet(JsonObject body, String field, String column, List<String> sets, List<Object> values) {
        if (body.has(field)) { sets.add(column + " = ?"); values.add(required(body, field)); }
    }

    private static long id(String value) {
        try { long id = Long.parseLong(value); if (id <= 0) throw new NumberFormatException(); return id; }
        catch (NumberFormatException exception) { throw new BadRequest("ID must be a positive number."); }
    }

    private static String[] path(URI uri) {
        String path = uri.getPath().substring("/api".length());
        if (path.startsWith("/")) path = path.substring(1);
        return path.isBlank() ? new String[0] : Arrays.stream(path.split("/")).filter(s -> !s.isBlank()).toArray(String[]::new);
    }

    private static Map<String, String> query(URI uri) {
        Map<String, String> result = new HashMap<>();
        String raw = uri.getRawQuery(); if (raw == null) return result;
        for (String pair : raw.split("&")) { String[] values = pair.split("=", 2); if (values.length == 2) result.put(URLDecoder.decode(values[0], StandardCharsets.UTF_8), URLDecoder.decode(values[1], StandardCharsets.UTF_8)); }
        return result;
    }

    private static JsonObject object(String name, Object value) {
        JsonObject result = new JsonObject(); if (value instanceof Number number) result.addProperty(name, number); else result.addProperty(name, String.valueOf(value)); return result;
    }

    private static JsonObject userJson(User user) {
        JsonObject result = new JsonObject(); result.addProperty("id", user.getId()); result.addProperty("email", user.getEmail());
        result.addProperty("fullName", user.getFullName()); result.addProperty("role", user.getRole().name()); return result;
    }

    private static JsonArray restaurantsJson(List<Restaurant> values) { JsonArray result = new JsonArray(); values.forEach(value -> result.add(restaurantJson(value))); return result; }
    private static JsonObject restaurantJson(Restaurant value) {
        JsonObject result = new JsonObject(); result.addProperty("id", value.id()); result.addProperty("name", value.name()); result.addProperty("description", value.description());
        result.addProperty("cuisine", value.cuisine()); result.addProperty("location", value.location()); result.addProperty("rating", value.rating());
        result.addProperty("deliveryTimeMinutes", value.deliveryTimeMinutes()); result.addProperty("deliveryCharge", value.deliveryCharge()); result.addProperty("status", value.status()); return result;
    }
    private static JsonArray menuJson(List<MenuItem> values) { JsonArray result = new JsonArray(); values.forEach(value -> result.add(menu(value))); return result; }
    private static JsonArray cartJson(Cart value) {
        JsonArray result = new JsonArray();
        value.items().forEach(item -> {
            JsonObject row = new JsonObject();
            row.addProperty("itemId", item.itemId());
            row.addProperty("restaurantId", item.restaurantId());
            row.addProperty("restaurantName", item.restaurantName());
            row.addProperty("itemName", item.itemName());
            row.addProperty("unitPrice", item.unitPrice());
            row.addProperty("quantity", item.quantity());
            row.addProperty("available", item.available());
            result.add(row);
        });
        return result;
    }
    private static JsonObject menu(MenuItem value) {
        JsonObject result = new JsonObject(); result.addProperty("id", value.id()); result.addProperty("restaurantId", value.restaurantId()); result.addProperty("categoryId", value.categoryId()); result.addProperty("category", value.category());
        result.addProperty("name", value.name()); result.addProperty("description", value.description()); result.addProperty("price", value.price());
        result.addProperty("discountPrice", value.discountPrice()); result.addProperty("preparationTimeMinutes", value.preparationTimeMinutes()); result.addProperty("isVeg", value.isVeg());
        result.addProperty("availability", value.availability()); result.addProperty("available", value.available()); result.addProperty("stock", value.stock()); return result;
    }
    private static JsonObject restaurantRow(ResultSet r) throws SQLException {
        JsonObject row = new JsonObject(); row.addProperty("id", r.getLong(1)); row.addProperty("ownerId", r.getLong(2)); row.addProperty("name", r.getString(3));
        row.addProperty("description", r.getString(4)); row.addProperty("cuisine", r.getString(5)); row.addProperty("location", r.getString(6)); row.addProperty("contactPhone", r.getString(7));
        row.addProperty("openingHours", r.getString(8)); row.addProperty("rating", r.getBigDecimal(9)); row.addProperty("deliveryTimeMinutes", r.getInt(10));
        row.addProperty("deliveryCharge", r.getBigDecimal(12)); row.addProperty("status", r.getString(13)); return row;
    }
    private static JsonObject menuRow(ResultSet r) throws SQLException {
        JsonObject row = new JsonObject(); row.addProperty("id", r.getLong(1)); row.addProperty("restaurantId", r.getLong(2)); row.addProperty("categoryId", r.getLong(3));
        row.addProperty("category", r.getString(4)); row.addProperty("name", r.getString(5)); row.addProperty("description", r.getString(6)); row.addProperty("price", r.getBigDecimal(7));
        String availability = r.getString(11);
        row.addProperty("discountPrice", r.getBigDecimal(8)); row.addProperty("preparationTimeMinutes", r.getInt(9)); row.addProperty("isVeg", r.getBoolean(10));
        row.addProperty("availability", availability); row.addProperty("available", "AVAILABLE".equals(availability)); row.addProperty("stock", r.getInt(13)); return row;
    }
    private static JsonObject orderSummary(OrderSummary order) {
        JsonObject result = new JsonObject(); result.addProperty("id", order.id()); result.addProperty("restaurantName", order.restaurantName());
        result.addProperty("orderDate", order.orderDate().toString()); result.addProperty("status", order.status().name()); result.addProperty("totalAmount", order.totalAmount()); return result;
    }
    private static void addCors(HttpExchange exchange) { exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization"); exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"); }
    private static void sendError(HttpExchange exchange, int status, String message) { JsonObject error = new JsonObject(); error.addProperty("error", message); send(exchange, status, error); }
    private static void send(HttpExchange exchange, int status, JsonElement data) {
        try {
            if (status == 204) {
                exchange.sendResponseHeaders(status, -1);
                exchange.close();
                return;
            }
            JsonObject response = new JsonObject(); response.addProperty("success", status < 400);
            if (data != null) response.add("data", data);
            byte[] bytes = GSON.toJson(response).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream output = exchange.getResponseBody()) { output.write(bytes); }
        }
        catch (IOException exception) { System.err.println("FoodOS API response could not be written: " + exception.getMessage()); }
    }

    private static class BadRequest extends RuntimeException { BadRequest(String message) { super(message); } }
    private static class Unauthorized extends RuntimeException { Unauthorized(String message) { super(message); } }
    private static class Forbidden extends RuntimeException { Forbidden(String message) { super(message); } }
    private static class NotFound extends RuntimeException { NotFound() { super("Resource not found."); } }
    private static class Conflict extends RuntimeException { Conflict(String message) { super(message); } }
}
