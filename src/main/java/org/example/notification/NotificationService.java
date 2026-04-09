package org.example.notification;

/**
 * Defines the operations for notification delivery.
 */
public interface NotificationService {

    /**
     * Sends a notification to the given recipient.
     *
     * @param to recipient address
     * @param subject subject text to show or send
     * @param body message text to show or send
     */
    void send(String to, String subject, String body);
}

