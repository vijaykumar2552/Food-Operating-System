// Defines the typed FoodOS role model used across application layers.
// Encapsulates domain data so services and DAOs share a consistent shape.

package com.foodapp.model;

public enum Role {
    CUSTOMER, RESTAURANT_OWNER, RESTAURANT_STAFF, ADMIN
}
