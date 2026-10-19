// Defines the typed FoodOS order detail model used across application layers.
// Encapsulates domain data so services and DAOs share a consistent shape.

package com.foodapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetail(long id, String restaurantName, LocalDateTime orderDate,
                          OrderStatus status, BigDecimal subtotal, BigDecimal discount,
                          BigDecimal deliveryFee, BigDecimal totalAmount, List<OrderLine> items) {
    public OrderDetail { items = List.copyOf(items); }
    public record OrderLine(String name, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) { }
}
