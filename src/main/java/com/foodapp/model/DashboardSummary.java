// Defines the typed FoodOS dashboard summary model used across application layers.
// Encapsulates domain data so services and DAOs share a consistent shape.

package com.foodapp.model;

public record DashboardSummary(long restaurantCount, long activeOrders, long recentOrders) { }
