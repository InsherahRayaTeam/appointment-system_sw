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
        System.out.println("[AUTH] Login successful at " + now());
    }

    /**
     * Logs a failed login event.
     *
     * @param username user involved in this action
     */
    public void logLoginFailure(String username) {
        String inputStatus = (username == null || username.trim().isEmpty()) ? "blank input" : "provided input";
        System.out.println("[AUTH] Login failed with " + inputStatus + " at " + now());
    }

    /**
     * Logs a logout event.
     *
     * @param username user involved in this action
     */
    public void logLogout(String username) {
        System.out.println("[AUTH] Logout at " + now());
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
