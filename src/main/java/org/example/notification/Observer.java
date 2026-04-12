package org.example.notification;

/**
 * Contract for classes that receive text notifications.
 */
public interface Observer {

    /**
     * Handles a new notification message.
     *
     * @param message message text to show or send
     */
    void update(String message);
}
