// Represents a typed FoodOS insufficient inventory exception application failure.
// Exception types let callers handle errors without exposing internal details.

package com.foodapp.exception;

public class InsufficientInventoryException extends AppException {
    public InsufficientInventoryException(String message) { super(message); }
}
