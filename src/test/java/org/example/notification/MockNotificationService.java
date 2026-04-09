package org.example.notification;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents mock notification service in the system.
 */
public class MockNotificationService implements NotificationService {

    private final List<String> sentMessages = new ArrayList<>();

    /**
     * Sends a notification to the given recipient.
     *
     * @param to recipient address
     * @param subject subject text to show or send
     * @param body message text to show or send
     */
    @Override
    public void send(String to, String subject, String body) {
        sentMessages.add(to + "|" + subject + "|" + body);
    }

    /**
     * Returns the sent messages.
     *
     * @return collection with the requested results
     */
    public List<String> getSentMessages() {
        return new ArrayList<>(sentMessages);
    }
}

