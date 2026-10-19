// Manages bearer-token sessions used by authenticated API requests.
// Encapsulates token lookup and revocation for the HTTP layer.

package com.foodapp.api;

import com.foodapp.model.User;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class SessionStore {
    private static final Duration SESSION_DURATION = Duration.ofHours(8);
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    String create(User user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new Session(user, Instant.now().plus(SESSION_DURATION)));
        return token;
    }

    User find(String token) {
        if (token == null || token.isBlank()) return null;
        Session session = sessions.get(token);
        if (session == null) return null;
        if (session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(token);
            return null;
        }
        return session.user();
    }

    void revoke(String token) {
        if (token != null) sessions.remove(token);
    }

    private record Session(User user, Instant expiresAt) { }
}
