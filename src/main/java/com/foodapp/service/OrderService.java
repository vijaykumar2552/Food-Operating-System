// Applies FoodOS order service business rules for API and desktop workflows.
// Coordinates validation and persistence through the service layer.

package com.foodapp.service;

import com.foodapp.dao.*;
import com.foodapp.exception.*;
import com.foodapp.model.*;
import com.foodapp.util.Database;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class OrderService {
    private final Database database; private final CartDao cartDao; private final MenuItemDao menuDao; private final OrderDao orderDao; private final AddressDao addressDao;
    public OrderService(Database database, CartDao cartDao, MenuItemDao menuDao, OrderDao orderDao, AddressDao addressDao) { this.database = database; this.cartDao = cartDao; this.menuDao = menuDao; this.orderDao = orderDao; this.addressDao = addressDao; }

    public record CheckoutLine(long itemId, int quantity) { }

    public long checkout(long customerId, String addressLine, String city, String postalCode, PaymentMethod method) throws AppException {
                validateRequest(customerId, addressLine, city, postalCode, method);
                try (Connection connection = database.openConnection()) {
                    connection.setAutoCommit(false);
                    try {
                        List<CartItem> cartItems = cartDao.findItems(connection, customerId);
                        if (cartItems.isEmpty()) throw new InvalidOrderException("Your cart is empty.");
                        cartItems = cartItems.stream().sorted(Comparator.comparingLong(CartItem::itemId)).toList();
                        cartDao.clear(connection, customerId);
                        return checkout(connection, customerId, cartItems.stream().map(item -> new CheckoutLine(item.itemId(), item.quantity())).toList(),
                                addressLine, city, postalCode, method);
                    } catch (AppException | SQLException exception) { try { connection.rollback(); } catch (SQLException rollbackException) { exception.addSuppressed(rollbackException); } if (exception instanceof AppException appException) throw appException; throw new DatabaseOperationException("Checkout failed and was rolled back.", exception); } finally { connection.setAutoCommit(true); }
                } catch (SQLException exception) { throw new DatabaseOperationException("Checkout could not connect to the database.", exception); }
    }

    public long checkout(long customerId, List<CheckoutLine> lines, String addressLine, String city,
                                 String postalCode, PaymentMethod method) throws AppException {
                validateRequest(customerId, addressLine, city, postalCode, method);
                validateLines(lines);
                try (Connection connection = database.openConnection()) {
                    connection.setAutoCommit(false);
                    try {
                        return checkout(connection, customerId, lines, addressLine, city, postalCode, method);
                    } catch (AppException | SQLException exception) { try { connection.rollback(); } catch (SQLException rollbackException) { exception.addSuppressed(rollbackException); } if (exception instanceof AppException appException) throw appException; throw new DatabaseOperationException("Checkout failed and was rolled back.", exception); } finally { connection.setAutoCommit(true); }
                } catch (SQLException exception) { throw new DatabaseOperationException("Checkout could not connect to the database.", exception); }
    }

    private long checkout(Connection connection, long customerId, List<CheckoutLine> inputLines,
                                  String addressLine, String city, String postalCode, PaymentMethod method) throws SQLException, AppException {
                validateLines(inputLines);
                List<CheckoutLine> lines = inputLines.stream().sorted(Comparator.comparingLong(CheckoutLine::itemId)).toList();
                Set<Long> itemIds = new HashSet<>();
                for (CheckoutLine line : lines) {
                    if (line == null || line.itemId() <= 0 || line.quantity() < 1 || line.quantity() > 50) throw new InvalidOrderException("Cart quantity is invalid.");
                    if (!itemIds.add(line.itemId())) throw new InvalidOrderException("A menu item was submitted more than once.");
                }
                long restaurantId = 0;
                BigDecimal subtotal = BigDecimal.ZERO;
                List<MenuItem> lockedItems = new java.util.ArrayList<>();
                for (CheckoutLine line : lines) {
                    MenuItem locked = menuDao.lockForCheckout(connection, line.itemId());
                    if (locked == null || !locked.available() || (restaurantId != 0 && locked.restaurantId() != restaurantId)) throw new InvalidOrderException("A cart item is no longer available.");
                    if (restaurantId == 0) restaurantId = locked.restaurantId();
                    if (locked.stock() < line.quantity()) throw new InsufficientInventoryException("Not enough stock for " + locked.name() + ".");
                    BigDecimal unitPrice = locked.discountPrice() != null && locked.discountPrice().compareTo(locked.price()) < 0 ? locked.discountPrice() : locked.price();
                    subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(line.quantity())));
                    lockedItems.add(locked);
                    menuDao.decreaseInventory(connection, locked.id(), line.quantity());
                }
                BigDecimal deliveryCharge;
                try (var statement = connection.prepareStatement("SELECT delivery_charge FROM restaurants WHERE restaurant_id = ? AND status = 'ACTIVE' FOR SHARE")) {
                    statement.setLong(1, restaurantId);
                    try (var result = statement.executeQuery()) {
                        if (!result.next()) throw new InvalidOrderException("The restaurant is no longer available.");
                        deliveryCharge = result.getBigDecimal(1);
                    }
                }
                BigDecimal discount = subtotal.compareTo(new BigDecimal("1000.00")) >= 0 ? subtotal.multiply(new BigDecimal("0.10")) : BigDecimal.ZERO;
                BigDecimal delivery = subtotal.compareTo(new BigDecimal("500.00")) >= 0 ? BigDecimal.ZERO : deliveryCharge;
                discount = discount.setScale(2, RoundingMode.HALF_UP);
                BigDecimal total = subtotal.subtract(discount).add(delivery).setScale(2, RoundingMode.HALF_UP);
                long addressId = addressDao.findOrCreateDefault(connection, customerId, addressLine.trim(), city.trim(), postalCode.trim());
                long orderId = orderDao.createOrder(connection, customerId, restaurantId, addressId, subtotal, discount, delivery, total);
                for (int index = 0; index < lines.size(); index++) {
                    BigDecimal unitPrice = lockedItems.get(index).discountPrice() != null && lockedItems.get(index).discountPrice().compareTo(lockedItems.get(index).price()) < 0
                            ? lockedItems.get(index).discountPrice() : lockedItems.get(index).price();
                    orderDao.addOrderItem(connection, orderId, lines.get(index).itemId(), lines.get(index).quantity(), unitPrice);
                }
                orderDao.createPayment(connection, orderId, method, total);
                connection.commit();
                return orderId;
    }

    private static void validateRequest(long customerId, String addressLine, String city,
                                        String postalCode, PaymentMethod method) throws InvalidOrderException {
        if (customerId <= 0) throw new InvalidOrderException("Please sign in before checkout.");
        if (method == null) throw new InvalidOrderException("Choose a payment method.");
        if (isInvalidAddress(addressLine, 255) || isInvalidAddress(city, 80) || isInvalidAddress(postalCode, 15)) {
            throw new InvalidOrderException("Complete the delivery address.");
        }
    }

    private static boolean isInvalidAddress(String value, int maxLength) {
        return value == null || value.isBlank() || value.trim().length() > maxLength;
    }

    private static void validateLines(List<CheckoutLine> lines) throws InvalidOrderException {
        if (lines == null || lines.isEmpty()) throw new InvalidOrderException("Your cart is empty.");
        Set<Long> itemIds = new HashSet<>();
        for (CheckoutLine line : lines) {
            if (line == null || line.itemId() <= 0 || line.quantity() < 1 || line.quantity() > 50) {
                throw new InvalidOrderException("Cart quantity is invalid.");
            }
            if (!itemIds.add(line.itemId())) throw new InvalidOrderException("A menu item was submitted more than once.");
        }
    }
    public List<OrderSummary> history(long customerId) throws DatabaseOperationException { try (Connection connection = database.openConnection()) { return orderDao.findByCustomer(connection, customerId); } catch (SQLException exception) { throw new DatabaseOperationException("Unable to load orders.", exception); } }
    public OrderDetail detail(long customerId, long orderId) throws AppException { try (Connection connection = database.openConnection()) { OrderDetail detail = orderDao.findDetail(connection, customerId, orderId); if (detail == null) throw new OrderNotFoundException("Order was not found."); return detail; } catch (OrderNotFoundException exception) { throw exception; } catch (SQLException exception) { throw new DatabaseOperationException("Unable to load order details.", exception); } }
}
