package org.example.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuthEventLogger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void logLoginSuccess(String username) {
        System.out.println("[AUTH] Login successful for user '" + username + "' at " + now());
    }

    public void logLoginFailure(String username) {
        String displayUser = (username == null || username.trim().isEmpty()) ? "<blank>" : username.trim();
        System.out.println("[AUTH] Login failed for user '" + displayUser + "' at " + now());
    }

    public void logLogout(String username) {
        String displayUser = username == null ? "<unknown>" : username;
        System.out.println("[AUTH] Logout for user '" + displayUser + "' at " + now());
    }

    private String now() {
        return LocalDateTime.now().format(FORMATTER);
    }
}
