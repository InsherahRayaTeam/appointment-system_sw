package org.example.notification;

/**
 * Defines the operations for observer.
 */
public interface Observer {

    /**
     * Runs update for this class.
     *
     * @param message message text to show or send
     */
    void update(String message);
}
