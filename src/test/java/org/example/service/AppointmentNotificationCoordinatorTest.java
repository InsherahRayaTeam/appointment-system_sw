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
        assertTrue(message.contains("2026-04-15"));
        assertTrue(message.contains("10:30"));
    }

    @Test
    void sendApprovedNotification_RecordsOneMessage() {
        Appointment appointment = appointmentAt("bob@example.com", LocalDateTime.of(2026, 4, 16, 11, 45));

        coordinator.sendApprovedNotification(appointment);

        assertEquals(1, mockNotificationService.getSentMessages().size());
        String message = mockNotificationService.getSentMessages().get(0);
        assertTrue(message.contains("bob@example.com"));
        assertTrue(message.contains("Appointment Approved"));
        assertTrue(message.contains("2026-04-16"));
        assertTrue(message.contains("11:45"));
    }

    @Test
    void sendCancelledNotification_RecordsOneMessage() {
        Appointment appointment = appointmentAt("carol@example.com", LocalDateTime.of(2026, 4, 17, 14, 0));

        coordinator.sendCancelledNotification(appointment);

        assertEquals(1, mockNotificationService.getSentMessages().size());
        String message = mockNotificationService.getSentMessages().get(0);
        assertTrue(message.contains("carol@example.com"));
        assertTrue(message.contains("Appointment Cancelled"));
        assertTrue(message.contains("2026-04-17"));
        assertTrue(message.contains("14:00"));
    }

    @Test
    void sendModifiedNotification_RecordsOneMessage() {
        Appointment appointment = appointmentAt("dave@example.com", LocalDateTime.of(2026, 4, 18, 9, 15));

        coordinator.sendModifiedNotification(appointment);

        assertEquals(1, mockNotificationService.getSentMessages().size());
        String message = mockNotificationService.getSentMessages().get(0);
        assertTrue(message.contains("dave@example.com"));
        assertTrue(message.contains("Appointment Updated"));
        assertTrue(message.contains("2026-04-18"));
        assertTrue(message.contains("09:15"));
    }

    @Test
    void sendAttendedNotification_RecordsOneMessage() {
        Appointment appointment = appointmentAt("erin@example.com", LocalDateTime.of(2026, 4, 19, 8, 0));

        coordinator.sendAttendedNotification(appointment);

        assertEquals(1, mockNotificationService.getSentMessages().size());
        String message = mockNotificationService.getSentMessages().get(0);
        assertTrue(message.contains("erin@example.com"));
        assertTrue(message.contains("Appointment Marked as Attended"));
        assertTrue(message.contains("Booked for:"));
    }

    @Test
    void sendCompletedNotification_RecordsOneMessage() {
        Appointment appointment = appointmentAt("frank@example.com", LocalDateTime.of(2026, 4, 20, 16, 45));

        coordinator.sendCompletedNotification(appointment);

        assertEquals(1, mockNotificationService.getSentMessages().size());
        String message = mockNotificationService.getSentMessages().get(0);
        assertTrue(message.contains("frank@example.com"));
        assertTrue(message.contains("Appointment Completed"));
        assertTrue(message.contains("Booked for:"));
    }

    @Test
    void sendRescheduledNotification_IncludesOldAndNewDateTimeWithAppointmentId() {
        Appointment previous = appointmentAt("erin@example.com", LocalDateTime.of(2026, 5, 2, 9, 0));
        Appointment updated = new Appointment(
                "apt-res-1",
                "erin@example.com",
                LocalDateTime.of(2026, 5, 4, 13, 30),
                60,
                1,
                AppointmentStatus.MODIFIED
        );

        coordinator.sendRescheduledNotification(previous, updated);

        assertEquals(1, mockNotificationService.getSentMessages().size());
        String message = mockNotificationService.getSentMessages().get(0);
        assertTrue(message.contains("erin@example.com"));
        assertTrue(message.contains("Appointment Rescheduled"));
        assertTrue(message.contains("Appointment ID: apt-res-1"));
        assertTrue(message.contains("Old date/time:"));
        assertTrue(message.contains("New date/day/time:"));
        assertTrue(message.contains("09:00"));
        assertTrue(message.contains("13:30"));
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

