// Represents a typed FoodOS authentication exception application failure.
// Exception types let callers handle errors without exposing internal details.

package com.foodapp.exception;

public class AuthenticationException extends AppException {
    public AuthenticationException(String message) { super(message); }
}
