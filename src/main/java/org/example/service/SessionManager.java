package org.example.service;

import org.example.domain.UserRole;
import org.example.domain.AdminUser;

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
    private AdminUser currentUser;
    private String currentUsername;
    private UserRole currentUserRole;
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
        login(username, UserRole.ADMIN);
    }

    /**
     * Starts an authenticated session for a username and role.
     *
     * @param username username to attach to the session
     * @param role authenticated role
     */
    public void login(String username, UserRole role) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("username cannot be blank");
        }
        if (role == null) {
            throw new IllegalArgumentException("role cannot be null");
        }

        loggedIn = true;
        currentUser = new AdminUser(username.trim() + "-session", username.trim(), "", role);
        currentUsername = username.trim();
        currentUserRole = role;
        loginTime = LocalDateTime.now();
    }

    /**
     * Starts an authenticated session for a user object.
     *
     * @param user authenticated user
     */
    public void login(AdminUser user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("user cannot be null or blank");
        }
        UserRole role = user.getRole() == null ? UserRole.USER : user.getRole();
        loggedIn = true;
        currentUser = user;
        currentUsername = user.getUsername().trim();
        currentUserRole = role;
        loginTime = LocalDateTime.now();
    }

    /**
     * Backward-compatible login API.
     */
    public void login() {
        login("admin", UserRole.ADMIN);
    }

    /**
     * Clears the in-memory session state.
     */
    public void logout() {
        loggedIn = false;
        currentUser = null;
        currentUsername = null;
        currentUserRole = null;
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
     * Returns the current authenticated user.
     *
     * @return current user or null when logged out
     */
    public AdminUser getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns the timestamp when the current session started.
     *
     * @return session login timestamp, or null when logged out
     */
    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    /**
     * Returns the role for the current session.
     *
     * @return authenticated role or null when logged out
     */
    public UserRole getCurrentUserRole() {
        return currentUserRole;
    }

    /**
     * Indicates whether current session belongs to an administrator.
     *
     * @return true for admin session
     */
    public boolean isAdmin() {
        return isLoggedIn() && currentUserRole == UserRole.ADMIN;
    }

    /**
     * Indicates whether current session belongs to a regular user.
     *
     * @return true for regular user session
     */
    public boolean isUser() {
        return isLoggedIn() && currentUserRole == UserRole.USER;
    }
}
