// Represents a typed FoodOS authorization exception application failure.
// Exception types let callers handle errors without exposing internal details.

package com.foodapp.exception;

public class AuthorizationException extends AppException {
    public AuthorizationException(String message) { super(message); }
}
