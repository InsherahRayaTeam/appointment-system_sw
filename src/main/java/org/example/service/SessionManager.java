package org.example.service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Tracks authenticated session state and emits logout notifications through services.
 *
 * @author appointment-system
 * @version 1.0
 */
public class SessionManager {

    private boolean loggedIn = false;
    private String currentUsername;
    private LocalDateTime loginTime;
    private final AuthEventLogger authEventLogger;
    private final EventManager eventManager;

    /**
     * Creates a session manager with mandatory logout collaborators.
     *
     * @param authEventLogger logger used for logout audit events
     * @param eventManager event manager used to publish logout notifications
     */
    public SessionManager(AuthEventLogger authEventLogger, EventManager eventManager) {
        this.authEventLogger = Objects.requireNonNull(authEventLogger, "authEventLogger cannot be null");
        this.eventManager = Objects.requireNonNull(eventManager, "eventManager cannot be null");
    }

    /**
     * Starts an authenticated session for a username.
     *
     * @param username username to attach to the session
     */
    public void login(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("username cannot be blank");
        }

        loggedIn = true;
        currentUsername = username.trim();
        loginTime = LocalDateTime.now();
    }

    /**
     * Backward-compatible login API.
     */
    public void login() {
        login("admin");
    }

    /**
     * Clears the in-memory session state.
     */
    public void logout() {
        loggedIn = false;
        currentUsername = null;
        loginTime = null;
    }

    /**
     * Clears session state and publishes logout logging/notification side effects.
     */
    public void logoutAndNotify() {
        String username = currentUsername;
        logout();

        authEventLogger.logLogout(username);
        String displayUser = username == null ? "<unknown>" : username;
        eventManager.notifyObservers("Goodbye, " + displayUser + "! You have been logged out.");
    }

    /**
     * Indicates whether a user is currently logged in.
     *
     * @return true when session is active, otherwise false
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Returns the username for the current session.
     *
     * @return current username or null when logged out
     */
    public String getCurrentUsername() {
        return currentUsername;
    }

    /**
     * Returns the timestamp when the current session started.
     *
     * @return session login timestamp, or null when logged out
     */
    public LocalDateTime getLoginTime() {
        return loginTime;
    }
}
