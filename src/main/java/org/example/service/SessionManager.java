package org.example.service;

import org.example.domain.SystemUser;
import org.example.domain.UserRole;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents session manager in the system.
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
     * Creates a new session manager object with the given values.
     *
     * @param authEventLogger value for auth event logger
     * @param eventManager manager object used for shared app state
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
     * Runs login for this class.
     *
     * @param email email address used for login or matching
     */
    public void login(String email) {
        login(email, UserRole.USER);
    }

    /**
     * Runs login for this class.
     *
     * @param email email address used for login or matching
     * @param role role value used for access control
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
     * Runs login for this class.
     *
     * @param user user involved in this action
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
     * Runs login for this class.
     */
    public void login() {
        login("admin@gmail.com", UserRole.ADMIN);
    }

    /**
     * Runs logout for this class.
     */
    public void logout() {
        loggedIn = false;
        currentUser = null;
        currentEmail = null;
        currentUserRole = null;
        loginTime = null;
    }

    /**
     * Runs logout and notify for this class.
     */
    public void logoutAndNotify() {
        String email = currentEmail;
        logout();

        authEventLogger.logLogout(email);
        String displayUser = email == null ? "<unknown>" : email;
        eventManager.notifyObservers("Goodbye, " + displayUser + "! You have been logged out.");
    }

    /**
     * Checks whether logged in is true.
     *
     * @return true when the action is valid or successful, otherwise false
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Returns the current email.
     *
     * @return text result from this method
     */
    public String getCurrentEmail() {
        return currentEmail;
    }

    /**
     * Returns the current user.
     *
     * @return requested value from this object
     */
    public SystemUser getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns the login time.
     *
     * @return requested value from this object
     */
    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    /**
     * Returns the current user role.
     *
     * @return requested value from this object
     */
    public UserRole getCurrentUserRole() {
        return currentUserRole;
    }

    /**
     * Checks whether admin is true.
     *
     * @return true when the action is valid or successful, otherwise false
     */
    public boolean isAdmin() {
        return isLoggedIn() && currentUserRole == UserRole.ADMIN;
    }

    /**
     * Checks whether user is true.
     *
     * @return true when the action is valid or successful, otherwise false
     */
    public boolean isUser() {
        return isLoggedIn() && currentUserRole == UserRole.USER;
    }
}
