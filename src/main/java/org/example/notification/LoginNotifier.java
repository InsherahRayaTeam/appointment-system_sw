package org.example.notification;

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
        System.out.println("Notification: " + message);
    }

    /**
     * Sends login success to listeners.
     *
     * @param username user involved in this action
     */
    public void notifyLoginSuccess(String username) {
        System.out.println("Login successful.");
    }

    /**
     * Sends login failure to listeners.
     *
     * @param username user involved in this action
     */
    public void notifyLoginFailure(String username) {
        System.out.println("Failed login attempt.");
    }

    /**
     * Sends logout to listeners.
     *
     * @param username user involved in this action
     */
    public void notifyLogout(String username) {
        System.out.println("You have been logged out successfully.");
    }
}
