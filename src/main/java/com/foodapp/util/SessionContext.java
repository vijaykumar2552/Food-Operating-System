// Provides shared FoodOS session context support used by multiple application layers.
// Centralizes reusable behavior instead of duplicating it in callers.

package com.foodapp.util;

import com.foodapp.model.User;

public final class SessionContext {
    private static User currentUser;
    private SessionContext() { }
    public static void signIn(User user) { currentUser = user; }
    public static User currentUser() { return currentUser; }
    public static void signOut() { currentUser = null; }
}
