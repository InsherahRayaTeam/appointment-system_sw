package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentSlot;
import org.example.domain.AppointmentStatus;
import org.example.notification.MockNotificationService;
import org.example.repository.AppointmentBookingRepository;
import org.example.repository.AppointmentRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests notification integration behavior inside appointment booking service.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentBookingServiceNotificationTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentBookingRepository appointmentBookingRepository;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventManager eventManager;

    private MockNotificationService mockNotificationService;
    private AppointmentBookingService appointmentBookingService;

    /**
     * Creates test data and service dependencies before each test.
     */
    @BeforeEach
    void setUp() {
        mockNotificationService = new MockNotificationService();
        AppointmentNotificationCoordinator coordinator = new AppointmentNotificationCoordinator(mockNotificationService);

        appointmentBookingService = new AppointmentBookingService(
                appointmentRepository,
                appointmentBookingRepository,
                sessionManager,
                userRepository,
                eventManager,
                coordinator
        );
    }

    /**
     * Verifies a successful booking triggers a pending notification.
     */
    @Test
    void bookAppointment_Success_SendsPendingNotification() {
        AppointmentSlot slot = new AppointmentSlot("10:00");
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        BookingStatus result = appointmentBookingService.bookAppointment("alice@example.com", "10:00", 60, 1);

        assertEquals(BookingStatus.SUCCESS, result);
        assertEquals(1, mockNotificationService.getSentMessages().size());
        String message = mockNotificationService.getSentMessages().get(0);
        assertTrue(message.contains("alice@example.com"));
        assertTrue(message.contains("Appointment Request Received"));
        assertTrue(message.contains("Appointment date/time:"));
    }

    /**
     * Verifies a successful admin approval triggers an approved notification.
     */
    @Test
    void approveAppointment_Success_SendsApprovedNotification() {
        Appointment appointment = futureAppointment("apt-approve", "alice@example.com", "10:00");
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);
        when(appointmentBookingRepository.findById(eq("apt-approve"))).thenReturn(Optional.of(appointment));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        BookingStatus result = appointmentBookingService.approveAppointment("apt-approve");

        assertEquals(BookingStatus.SUCCESS, result);
        assertEquals(1, mockNotificationService.getSentMessages().size());
        String message = mockNotificationService.getSentMessages().get(0);
        assertTrue(message.contains("alice@example.com"));
        assertTrue(message.contains("Appointment Approved"));
        assertTrue(message.contains("Appointment date/time:"));
    }

    /**
     * Verifies a successful admin cancellation triggers a cancelled notification.
     */
    @Test
    void cancelAppointment_Success_SendsCancelledNotification() {
        AppointmentSlot slot = new AppointmentSlot("10:00");
        slot.book();
        Appointment appointment = futureAppointment("apt-cancel", "alice@example.com", "10:00");

        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);
        when(appointmentBookingRepository.findById(eq("apt-cancel"))).thenReturn(Optional.of(appointment));
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        BookingStatus result = appointmentBookingService.cancelAppointment("apt-cancel");

        assertEquals(BookingStatus.SUCCESS, result);
        assertEquals(1, mockNotificationService.getSentMessages().size());
        String message = mockNotificationService.getSentMessages().get(0);
        assertTrue(message.contains("alice@example.com"));
        assertTrue(message.contains("Appointment Cancelled"));
        assertTrue(message.contains("Appointment date/time:"));
    }

    /**
     * Verifies a successful admin modification triggers an updated notification.
     */
    @Test
    void modifyAppointment_Success_SendsModifiedNotification() {
        AppointmentSlot oldSlot = new AppointmentSlot("10:00");
        oldSlot.book();
        AppointmentSlot newSlot = new AppointmentSlot("11:00");
        Appointment appointment = futureAppointment("apt-modify", "alice@example.com", "10:00");

        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);
        when(appointmentBookingRepository.findById(eq("apt-modify"))).thenReturn(Optional.of(appointment));
        when(appointmentRepository.findAll()).thenReturn(List.of(oldSlot, newSlot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        BookingStatus result = appointmentBookingService.modifyAppointment("apt-modify", "11:00");

        assertEquals(BookingStatus.SUCCESS, result);
        assertEquals(1, mockNotificationService.getSentMessages().size());
        String message = mockNotificationService.getSentMessages().get(0);
        assertTrue(message.contains("alice@example.com"));
        assertTrue(message.contains("Appointment Updated"));
        assertTrue(message.contains("Appointment date/time:"));
        assertTrue(message.contains("11:00"));
    }

    /**
     * Verifies no updated notification is sent when appointment persistence fails.
     */
    @Test
    void modifyAppointment_UpdateFails_DoesNotSendModifiedNotification() {
        AppointmentSlot oldSlot = new AppointmentSlot("10:00");
        oldSlot.book();
        AppointmentSlot newSlot = new AppointmentSlot("11:00");
        Appointment appointment = futureAppointment("apt-modify-fail", "alice@example.com", "10:00");

        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);
        when(appointmentBookingRepository.findById(eq("apt-modify-fail"))).thenReturn(Optional.of(appointment));
        when(appointmentRepository.findAll()).thenReturn(List.of(oldSlot, newSlot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(false);

        BookingStatus result = appointmentBookingService.modifyAppointment("apt-modify-fail", "11:00");

        assertEquals(BookingStatus.UPDATE_FAILED, result);
        assertTrue(mockNotificationService.getSentMessages().isEmpty());
        assertTrue(oldSlot.isBooked());
        assertFalse(newSlot.isBooked());
    }

    private Appointment futureAppointment(String id, String email, String slotTime) {
        return new Appointment(
                id,
                email,
                LocalDate.now().plusDays(1).atTime(LocalTime.parse(slotTime)),
                60,
                1,
                AppointmentStatus.CONFIRMED
        );
    }
}

