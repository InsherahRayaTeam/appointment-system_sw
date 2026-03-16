package org.example.notification;

/**
 * Sends user-facing notifications for login and logout events.
 * Complements AuthEventLogger (audit log) with friendly console messages.
 */
public class LoginNotifier {

    public void notifyLoginSuccess(String username) {
        System.out.println("[NOTIFY] Welcome, " + username + "! You are now logged in.");
    }

    public void notifyLoginFailure(String username) {
        String displayUser = (username == null || username.trim().isEmpty()) ? "<blank>" : username.trim();
        System.out.println("[NOTIFY] Failed login attempt detected for user '" + displayUser + "'.");
    }

    public void notifyLogout(String username) {
        String displayUser = username == null ? "<unknown>" : username;
        System.out.println("[NOTIFY] Goodbye, " + displayUser + "! You have been logged out.");
    }
}

