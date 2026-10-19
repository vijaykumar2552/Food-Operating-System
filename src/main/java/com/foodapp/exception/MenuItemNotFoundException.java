// Represents a typed FoodOS menu item not found exception application failure.
// Exception types let callers handle errors without exposing internal details.

package com.foodapp.exception;

public class MenuItemNotFoundException extends AppException {
    public MenuItemNotFoundException(String message) { super(message); }
}
