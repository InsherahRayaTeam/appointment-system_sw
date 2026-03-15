package org.example.service;

import java.time.LocalDateTime;

public class SessionManager {

    private boolean loggedIn = false;
    private String currentUsername;
    private LocalDateTime loginTime;

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

    public void logout() {
        loggedIn = false;
        currentUsername = null;
        loginTime = null;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }
}
