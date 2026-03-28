package org.example.notification;

/**
 * Observer contract for receiving notification messages.
 *
 * @author appointment-system
 * @version 1.0
 */
public interface Observer {

    /**
     * Receives an event message from the subject.
     *
     * @param message event message
     */
    void update(String message);
}