// Defines the typed FoodOS order summary model used across application layers.
// Encapsulates domain data so services and DAOs share a consistent shape.

package com.foodapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummary(long id, String restaurantName, LocalDateTime orderDate,
                           OrderStatus status, BigDecimal totalAmount) { }
