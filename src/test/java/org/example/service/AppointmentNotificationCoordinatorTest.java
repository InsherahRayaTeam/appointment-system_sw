package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentStatus;
import org.example.notification.MockNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppointmentNotificationCoordinatorTest {

    private MockNotificationService mockNotificationService;
    private AppointmentNotificationCoordinator coordinator;

    @BeforeEach
    void setUp() {
        mockNotificationService = new MockNotificationService();
        coordinator = new AppointmentNotificationCoordinator(mockNotificationService);
    }

    @Test
    void sendPendingNotification_RecordsOneMessage() {
        Appointment appointment = appointmentAt("alice@example.com", LocalDateTime.of(2026, 4, 15, 10, 30));

        coordinator.sendPendingNotification(appointment);

        assertEquals(1, mockNotificationService.getSentMessages().size());
        String message = mockNotificationService.getSentMessages().get(0);
        assertTrue(message.contains("alice@example.com"));
        assertTrue(message.contains("Appointment Request Received"));
        assertTrue(message.contains("2026-04-15 10:30"));
    }

    @Test
    void sendApprovedNotification_RecordsOneMessage() {
        Appointment appointment = appointmentAt("bob@example.com", LocalDateTime.of(2026, 4, 16, 11, 45));

        coordinator.sendApprovedNotification(appointment);

        assertEquals(1, mockNotificationService.getSentMessages().size());
        String message = mockNotificationService.getSentMessages().get(0);
        assertTrue(message.contains("bob@example.com"));
        assertTrue(message.contains("Appointment Approved"));
        assertTrue(message.contains("2026-04-16 11:45"));
    }

    @Test
    void sendCancelledNotification_RecordsOneMessage() {
        Appointment appointment = appointmentAt("carol@example.com", LocalDateTime.of(2026, 4, 17, 14, 0));

        coordinator.sendCancelledNotification(appointment);

        assertEquals(1, mockNotificationService.getSentMessages().size());
        String message = mockNotificationService.getSentMessages().get(0);
        assertTrue(message.contains("carol@example.com"));
        assertTrue(message.contains("Appointment Cancelled"));
        assertTrue(message.contains("2026-04-17 14:00"));
    }

    @Test
    void sendModifiedNotification_RecordsOneMessage() {
        Appointment appointment = appointmentAt("dave@example.com", LocalDateTime.of(2026, 4, 18, 9, 15));

        coordinator.sendModifiedNotification(appointment);

        assertEquals(1, mockNotificationService.getSentMessages().size());
        String message = mockNotificationService.getSentMessages().get(0);
        assertTrue(message.contains("dave@example.com"));
        assertTrue(message.contains("Appointment Updated"));
        assertTrue(message.contains("2026-04-18 09:15"));
    }

    private Appointment appointmentAt(String email, LocalDateTime dateTime) {
        return new Appointment(
                "apt-notification",
                email,
                dateTime,
                60,
                1,
                AppointmentStatus.CONFIRMED
        );
    }
}

