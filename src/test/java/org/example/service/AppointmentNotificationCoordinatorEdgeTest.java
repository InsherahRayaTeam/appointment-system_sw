package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentStatus;
import org.example.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppointmentNotificationCoordinatorEdgeTest {

    @Mock
    private NotificationService notificationService;

    private AppointmentNotificationCoordinator coordinator;

    @BeforeEach
    void setUp() {
        coordinator = new AppointmentNotificationCoordinator(notificationService);
    }

    @Test
    void sendPendingNotification_NullAppointment_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> coordinator.sendPendingNotification(null)
        );

        assertEquals("Appointment cannot be null", ex.getMessage());
    }

    @Test
    void sendApprovedNotification_AppointmentWithoutUser_ThrowsIllegalArgumentException() {
        Appointment appointment = new Appointment(
                "apt-no-user",
                null,
                null,
                LocalDateTime.now().plusDays(1),
                60,
                1,
                AppointmentStatus.CONFIRMED
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> coordinator.sendApprovedNotification(appointment)
        );

        assertEquals("Appointment user cannot be null", ex.getMessage());
    }

    @Test
    void sendRescheduledNotification_MissingIdNameAndDate_UsesNAFallbackValues() {
        Appointment previous = new Appointment(
                "old-id",
                "alice@example.com",
                null,
                60,
                1,
                AppointmentStatus.CONFIRMED
        );
        Appointment updated = new Appointment(
                null,
                "",
                "alice@example.com",
                null,
                60,
                1,
                AppointmentStatus.RESCHEDULED
        );

        coordinator.sendRescheduledNotification(previous, updated);

        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).send(org.mockito.ArgumentMatchers.eq("alice@example.com"), subjectCaptor.capture(), bodyCaptor.capture());

        assertEquals("Appointment Rescheduled", subjectCaptor.getValue());
        assertTrue(bodyCaptor.getValue().contains("Appointment ID: N/A"));
        assertTrue(bodyCaptor.getValue().contains("User: N/A"));
        assertTrue(bodyCaptor.getValue().contains("Old date/time: N/A"));
        assertTrue(bodyCaptor.getValue().contains("New date/day/time: N/A"));
    }

    @Test
    void sendNotAttendedNotification_ValidAppointment_ContainsRescheduleGuidanceText() {
        Appointment appointment = new Appointment(
                "apt-1",
                "alice@example.com",
                LocalDateTime.of(2030, 1, 20, 14, 30),
                60,
                1,
                AppointmentStatus.NOT_ATTENDED
        );

        coordinator.sendNotAttendedNotification(appointment);

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).send(
                org.mockito.ArgumentMatchers.eq("alice@example.com"),
                org.mockito.ArgumentMatchers.eq("Appointment Marked as Not Attended"),
                bodyCaptor.capture()
        );
        assertTrue(bodyCaptor.getValue().contains("Please contact the office if you need to reschedule."));
        assertTrue(bodyCaptor.getValue().contains("2030-01-20"));
        assertTrue(bodyCaptor.getValue().contains("14:30"));
    }
}

