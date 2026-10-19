// Defines the typed FoodOS user model used across application layers.
// Encapsulates domain data so services and DAOs share a consistent shape.

package com.foodapp.model;

public class User {
    private final long id;
    private final String email;
    private final String fullName;
    private final Role role;
    private final boolean active;

    public User(long id, String email, String fullName, Role role, boolean active) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
    }

    public long getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public Role getRole() { return role; }
    public boolean isActive() { return active; }
}
