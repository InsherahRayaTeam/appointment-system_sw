package org.example.service;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;

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
    private SystemUser currentUser;
    private String currentEmail;
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
        this.authEventLogger = Objects.requireNonNull(
                authEventLogger,
                "authEventLogger cannot be null"
        );
        this.eventManager = Objects.requireNonNull(
                eventManager,
                "eventManager cannot be null"
        );
    }

    /**
     * Starts an authenticated session for an email.
     *
     * @param email email to attach to the session
     */
    public void login(String email) {
        login(email, UserRole.USER);
    }

    /**
     * Starts an authenticated session for an email and role.
     *
     * @param email email to attach to the session
     * @param role authenticated role
     */
    public void login(String email, UserRole role) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("email cannot be blank");
        }
        if (role == null) {
            throw new IllegalArgumentException("role cannot be null");
        }

        String normalizedEmail = email.trim().toLowerCase();

        loggedIn = true;
        currentUser = new SystemUser(normalizedEmail + "-session", normalizedEmail, "[SESSION]", role);
        currentEmail = normalizedEmail;
        currentUserRole = role;
        loginTime = LocalDateTime.now();
    }

    /**
     * Starts an authenticated session for a user object.
     *
     * @param user authenticated user
     */
    public void login(SystemUser user) {
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("user cannot be null or have blank email");
        }

        UserRole role = user.getRole() == null ? UserRole.USER : user.getRole();

        loggedIn = true;
        currentUser = user;
        currentEmail = user.getEmail().trim().toLowerCase();
        currentUserRole = role;
        loginTime = LocalDateTime.now();
    }

    /**
     * Backward-compatible login API.
     */
    public void login() {
        login("admin@gmail.com", UserRole.ADMIN);
    }

    /**
     * Clears the in-memory session state.
     */
    public void logout() {
        loggedIn = false;
        currentUser = null;
        currentEmail = null;
        currentUserRole = null;
        loginTime = null;
    }

    /**
     * Clears session state and publishes logout logging/notification side effects.
     */
    public void logoutAndNotify() {
        String email = currentEmail;
        logout();

        authEventLogger.logLogout(email);
        String displayUser = email == null ? "<unknown>" : email;
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
     * Returns the email for the current session.
     *
     * @return current email or null when logged out
     */
    public String getCurrentEmail() {
        return currentEmail;
    }

    /**
     * Returns the current authenticated user.
     *
     * @return current user or null when logged out
     */
    public SystemUser getCurrentUser() {
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