package org.example.service;

import java.time.LocalDateTime;

public class SessionManager {

    private boolean loggedIn = false;
    private String currentUsername;
    private LocalDateTime loginTime;

    public void login(String username) {
        loggedIn = true;
        currentUsername = username;
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
