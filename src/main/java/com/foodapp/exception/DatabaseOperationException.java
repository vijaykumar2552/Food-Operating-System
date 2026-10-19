// Represents a typed FoodOS database operation exception application failure.
// Exception types let callers handle errors without exposing internal details.

package com.foodapp.exception;

public class DatabaseOperationException extends AppException {
    public DatabaseOperationException(String message, Throwable cause) { super(message, cause); }
}
