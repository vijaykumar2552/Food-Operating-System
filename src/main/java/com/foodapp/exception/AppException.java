// Represents a typed FoodOS app exception application failure.
// Exception types let callers handle errors without exposing internal details.

package com.foodapp.exception;

public class AppException extends Exception {
    public AppException(String message) { super(message); }
    public AppException(String message, Throwable cause) { super(message, cause); }
}
