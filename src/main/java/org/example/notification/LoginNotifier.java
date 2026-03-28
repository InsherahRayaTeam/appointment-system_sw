package org.example.notification;

/**
 * Sends user-facing notifications for login and logout events.
 * Complements AuthEventLogger (audit log) with friendly console messages.
 *
 * @author appointment-system
 * @version 1.0
 */
public class LoginNotifier implements Observer {

    /**
     * Prints a generic observer notification message.
     *
     * @param message event message text
     */
    @Override
    public void update(String message) {
        System.out.println("🔔 Notification: " + message);
    }

    /**
     * Prints login success message.
     *
     * @param username username that logged in
     */
    public void notifyLoginSuccess(String username) {
        System.out.println("[NOTIFY] Welcome, " + username + "! You are now logged in.");
    }

    /**
     * Prints login failure message.
     *
     * @param username username that failed login
     */
    public void notifyLoginFailure(String username) {
        String displayUser = (username == null || username.trim().isEmpty()) ? "<blank>" : username.trim();
        System.out.println("[NOTIFY] Failed login attempt detected for user '" + displayUser + "'.");
    }

    /**
     * Prints logout message.
     *
     * @param username username that logged out
     */
    public void notifyLogout(String username) {
        String displayUser = username == null ? "<unknown>" : username;
        System.out.println("[NOTIFY] Goodbye, " + displayUser + "! You have been logged out.");
    }
}