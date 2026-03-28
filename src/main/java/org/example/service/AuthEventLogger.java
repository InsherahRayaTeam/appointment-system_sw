package org.example.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logs authentication and session events to the console.
 * Provides audit trail for login/logout activities with timestamps.
 *
 * @author appointment-system
 * @version 1.0
 */
public class AuthEventLogger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Logs a successful login event with timestamp.
     *
     * @param username the administrator username that logged in
     */
    public void logLoginSuccess(String username) {
        System.out.println("[AUTH] Login successful for user '" + username + "' at " + now());
    }

    /**
     * Logs a failed login attempt with timestamp.
     *
     * @param username the username that failed to authenticate
     */
    public void logLoginFailure(String username) {
        String displayUser = (username == null || username.trim().isEmpty()) ? "<blank>" : username.trim();
        System.out.println("[AUTH] Login failed for user '" + displayUser + "' at " + now());
    }

    /**
     * Logs a logout event with timestamp.
     *
     * @param username the username that logged out
     */
    public void logLogout(String username) {
        String displayUser = username == null ? "<unknown>" : username;
        System.out.println("[AUTH] Logout for user '" + displayUser + "' at " + now());
    }

    private String now() {
        return LocalDateTime.now().format(FORMATTER);
    }
}
