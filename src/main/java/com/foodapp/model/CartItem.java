// Defines the typed FoodOS cart item model used across application layers.
// Encapsulates domain data so services and DAOs share a consistent shape.

package com.foodapp.model;

import java.math.BigDecimal;

public record CartItem(long itemId, long restaurantId, String restaurantName, String itemName,
                       BigDecimal unitPrice, int quantity, boolean available) {
    public BigDecimal subtotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
}
