// Defines the typed FoodOS admin dashboard summary model used across application layers.
// Encapsulates domain data so services and DAOs share a consistent shape.

package com.foodapp.model;

import java.math.BigDecimal;
import java.util.List;

public record AdminDashboardSummary(
        long restaurantCount,
        long menuItemCount,
        long orderCount,
        BigDecimal revenue,
        List<PopularItem> popularItems
) {
    public record PopularItem(long itemId, String name, long quantity) { }
}
