// Defines the persistence contract for FoodOS order dao data.
// Service code depends on this interface rather than JDBC details.

package com.foodapp.dao;

import com.foodapp.model.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface OrderDao {
    long createOrder(Connection connection, long customerId, long restaurantId, long addressId, java.math.BigDecimal subtotal, java.math.BigDecimal discount, java.math.BigDecimal deliveryFee, java.math.BigDecimal total) throws SQLException;
    void addOrderItem(Connection connection, long orderId, long itemId, int quantity, java.math.BigDecimal price) throws SQLException;
    void createPayment(Connection connection, long orderId, PaymentMethod method, java.math.BigDecimal amount) throws SQLException;
    List<OrderSummary> findByCustomer(Connection connection, long customerId) throws SQLException;
    OrderDetail findDetail(Connection connection, long customerId, long orderId) throws SQLException;
}
