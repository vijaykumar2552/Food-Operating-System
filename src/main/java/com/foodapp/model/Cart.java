// Defines the typed FoodOS cart model used across application layers.
// Encapsulates domain data so services and DAOs share a consistent shape.

package com.foodapp.model;

import java.math.BigDecimal;
import java.util.List;

public record Cart(List<CartItem> items) {
    public Cart { items = List.copyOf(items); }
    public BigDecimal subtotal() { return items.stream().map(CartItem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add); }
    public int totalQuantity() { return items.stream().mapToInt(CartItem::quantity).sum(); }
    public boolean isEmpty() { return items.isEmpty(); }
    public long restaurantId() { return items.isEmpty() ? 0 : items.get(0).restaurantId(); }
}
