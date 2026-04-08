package org.example.notification;

/**
 * Represents login notifier in the system.
 */
public class LoginNotifier implements Observer {

    /**
     * Runs update for this class.
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
        String displayUser = (username == null || username.trim().isEmpty()) ? "<blank>" : username.trim();
        System.out.println("Failed login attempt for user '" + displayUser + "'.");
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
