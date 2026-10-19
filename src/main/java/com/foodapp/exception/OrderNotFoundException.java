// Represents a typed FoodOS order not found exception application failure.
// Exception types let callers handle errors without exposing internal details.

package com.foodapp.exception;

public class OrderNotFoundException extends AppException {
    public OrderNotFoundException(String message) { super(message); }
}
