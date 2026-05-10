package org.example.notification;

import org.example.util.Console;

/**
 * Prints login-related notifications to the console.
 */
public class LoginNotifier implements Observer {

    /**
     * Handles generic observer messages.
     *
     * @param message message text to show or send
     */
    @Override
    public void update(String message) {
        Console.println("Notification: " + message);
    }

    /**
     * Sends login success to listeners.
     *
     * @param username user involved in this action
     */
    public void notifyLoginSuccess(String username) {
        Console.println("Login successful.");
    }

    /**
     * Sends login failure to listeners.
     *
     * @param username user involved in this action
     */
    public void notifyLoginFailure(String username) {
        String displayUser = (username == null || username.trim().isEmpty()) ? "<blank>" : username.trim();
        Console.println("Failed login attempt for user '" + displayUser + "'.");
    }

    /**
     * Sends logout to listeners.
     *
     * @param username user involved in this action
     */
    public void notifyLogout(String username) {
        Console.println("You have been logged out successfully.");
    }
}
