package org.example.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Writes login and logout events to the console.
 */
public class AuthEventLogger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Logs a successful login event.
     *
     * @param username user involved in this action
     */
    public void logLoginSuccess(String username) {
        System.out.println("[AUTH] Login successful for user '" + username + "' at " + now());
    }

    /**
     * Logs a failed login event.
     *
     * @param username user involved in this action
     */
    public void logLoginFailure(String username) {
        String displayUser = (username == null || username.trim().isEmpty()) ? "<blank>" : username.trim();
        System.out.println("[AUTH] Login failed for user '" + displayUser + "' at " + now());
    }

    /**
     * Logs a logout event.
     *
     * @param username user involved in this action
     */
    public void logLogout(String username) {
        String displayUser = username == null ? "<unknown>" : username;
        System.out.println("[AUTH] Logout for user '" + displayUser + "' at " + now());
    }

    /**
     * Returns the current timestamp in log format.
     *
     * @return text result from this method
     */
    private String now() {
        return LocalDateTime.now().format(FORMATTER);
    }
}
