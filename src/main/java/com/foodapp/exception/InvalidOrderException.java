// Represents a typed FoodOS invalid order exception application failure.
// Exception types let callers handle errors without exposing internal details.

package com.foodapp.exception;

public class InvalidOrderException extends AppException {
    public InvalidOrderException(String message) { super(message); }
}
