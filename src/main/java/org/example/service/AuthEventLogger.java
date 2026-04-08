package org.example.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents auth event logger in the system.
 */
public class AuthEventLogger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Runs log login success for this class.
     *
     * @param username user involved in this action
     */
    public void logLoginSuccess(String username) {
        System.out.println("[AUTH] Login successful for user '" + username + "' at " + now());
    }

    /**
     * Runs log login failure for this class.
     *
     * @param username user involved in this action
     */
    public void logLoginFailure(String username) {
        String displayUser = (username == null || username.trim().isEmpty()) ? "<blank>" : username.trim();
        System.out.println("[AUTH] Login failed for user '" + displayUser + "' at " + now());
    }

    /**
     * Runs log logout for this class.
     *
     * @param username user involved in this action
     */
    public void logLogout(String username) {
        String displayUser = username == null ? "<unknown>" : username;
        System.out.println("[AUTH] Logout for user '" + displayUser + "' at " + now());
    }

    /**
     * Runs now for this class.
     *
     * @return text result from this method
     */
    private String now() {
        return LocalDateTime.now().format(FORMATTER);
    }
}
