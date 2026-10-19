// Defines the typed FoodOS order status model used across application layers.
// Encapsulates domain data so services and DAOs share a consistent shape.

package com.foodapp.model;

public enum OrderStatus {
    PLACED, ACCEPTED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, REJECTED, CANCELLED
}
