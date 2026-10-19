// Defines the typed FoodOS menu item model used across application layers.
// Encapsulates domain data so services and DAOs share a consistent shape.

package com.foodapp.model;

import java.math.BigDecimal;

public record MenuItem(
        long id,
        long restaurantId,
        long categoryId,
        String category,
        String name,
        String description,
        BigDecimal price,
        BigDecimal discountPrice,
        int preparationTimeMinutes,
        boolean isVeg,
        String availability,
        String imagePath,
        boolean available,
        int stock
) {
    public boolean isOrderable() { return "AVAILABLE".equals(availability) && available; }

    public static MenuItem legacy(long id, long restaurantId, String category, String name,
                                  String description, BigDecimal price, boolean available, int stock) {
        return new MenuItem(id, restaurantId, 0, category, name, description, price, null,
                15, true, available ? "AVAILABLE" : "SOLD_OUT", null, available, stock);
    }
}
