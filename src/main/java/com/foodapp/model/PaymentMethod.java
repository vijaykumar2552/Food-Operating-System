// Defines the typed FoodOS payment method model used across application layers.
// Encapsulates domain data so services and DAOs share a consistent shape.

package com.foodapp.model;

public enum PaymentMethod {
    CASH, CARD_SIMULATED, UPI_SIMULATED
}
