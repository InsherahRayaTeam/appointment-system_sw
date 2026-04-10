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

    private static final String ALICE_EMAIL = "alice@example.com";
    private static final String TEN_AM = "10:00";
    private static final String ELEVEN_AM = "11:00";
    private static final String APPOINTMENT_ID_APPROVE = "apt-approve";
    private static final String APPOINTMENT_ID_CANCEL = "apt-cancel";
    private static final String APPOINTMENT_ID_MODIFY = "apt-modify";
    private static final String APPOINTMENT_ID_MODIFY_FAIL = "apt-modify-fail";
    private static final String APPOINTMENT_ID_ATTEND = "apt-attend";
    private static final String APPOINTMENT_ID_COMPLETE = "apt-complete";
    private static final String APPOINTMENT_ID_NOT_ATTENDED = "apt-not-attended";

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
        AppointmentSlot slot = new AppointmentSlot(TEN_AM);
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        BookingStatus result = appointmentBookingService.bookAppointment(ALICE_EMAIL, TEN_AM, 60, 1);

        assertEquals(BookingStatus.SUCCESS, result);
        assertEquals(1, mockNotificationService.getSentMessages().size());
        String message = mockNotificationService.getSentMessages().get(0);
        assertTrue(message.contains(ALICE_EMAIL));
        assertTrue(message.contains("Appointment Request Received"));
        assertTrue(message.contains("Appointment date/time:"));
    }

    /**
     * Verifies a successful admin approval triggers an approved notification.
     */
    @Test
    void approveAppointment_Success_SendsApprovedNotification() {
        Appointment appointment = futureAppointment(APPOINTMENT_ID_APPROVE);
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);
        when(appointmentBookingRepository.findById(eq(APPOINTMENT_ID_APPROVE))).thenReturn(Optional.of(appointment));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        BookingStatus result = appointmentBookingService.approveAppointment(APPOINTMENT_ID_APPROVE);

        assertEquals(BookingStatus.SUCCESS, result);
        assertEquals(1, mockNotificationService.getSentMessages().size());
        String message = mockNotificationService.getSentMessages().get(0);
        assertTrue(message.contains(ALICE_EMAIL));
        assertTrue(message.contains("Appointment Approved"));
        assertTrue(message.contains("Appointment date/time:"));
    }

    /**
     * Verifies a successful admin cancellation triggers a cancelled notification.
     */
    @Test
    void cancelAppointment_Success_SendsCancelledNotification() {
        AppointmentSlot slot = new AppointmentSlot(TEN_AM);
        slot.book();
        Appointment appointment = futureAppointment(APPOINTMENT_ID_CANCEL);

        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);
        when(appointmentBookingRepository.findById(eq(APPOINTMENT_ID_CANCEL))).thenReturn(Optional.of(appointment));
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        BookingStatus result = appointmentBookingService.cancelAppointment(APPOINTMENT_ID_CANCEL);

        assertEquals(BookingStatus.SUCCESS, result);
        assertEquals(1, mockNotificationService.getSentMessages().size());
        String message = mockNotificationService.getSentMessages().get(0);
        assertTrue(message.contains(ALICE_EMAIL));
        assertTrue(message.contains("Appointment Cancelled"));
        assertTrue(message.contains("Appointment date/time:"));
    }

    /**
     * Verifies a successful admin modification triggers a detailed reschedule notification.
     */
    @Test
    void modifyAppointment_Success_SendsModifiedNotification() {
        AppointmentSlot oldSlot = new AppointmentSlot(TEN_AM);
        oldSlot.book();
        AppointmentSlot newSlot = new AppointmentSlot(ELEVEN_AM);
        Appointment appointment = futureAppointment(APPOINTMENT_ID_MODIFY);

        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);
        when(appointmentBookingRepository.findById(eq(APPOINTMENT_ID_MODIFY))).thenReturn(Optional.of(appointment));
        when(appointmentRepository.findAll()).thenReturn(List.of(oldSlot, newSlot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        BookingStatus result = appointmentBookingService.modifyAppointment(APPOINTMENT_ID_MODIFY, ELEVEN_AM);

        assertEquals(BookingStatus.SUCCESS, result);
        assertEquals(1, mockNotificationService.getSentMessages().size());
        String message = mockNotificationService.getSentMessages().get(0);
        assertTrue(message.contains(ALICE_EMAIL));
        assertTrue(message.contains("Appointment Rescheduled"));
        assertTrue(message.contains("Appointment ID: " + APPOINTMENT_ID_MODIFY));
        assertTrue(message.contains("Old date/time:"));
        assertTrue(message.contains("New date/day/time:"));
        assertTrue(message.contains(ELEVEN_AM));
        assertTrue(message.contains("cancel this reservation"));
    }

    /**
     * Verifies no updated notification is sent when appointment persistence fails.
     */
    @Test
    void modifyAppointment_UpdateFails_DoesNotSendModifiedNotification() {
        AppointmentSlot oldSlot = new AppointmentSlot(TEN_AM);
        oldSlot.book();
        AppointmentSlot newSlot = new AppointmentSlot(ELEVEN_AM);
        Appointment appointment = futureAppointment(APPOINTMENT_ID_MODIFY_FAIL);

        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);
        when(appointmentBookingRepository.findById(eq(APPOINTMENT_ID_MODIFY_FAIL))).thenReturn(Optional.of(appointment));
        when(appointmentRepository.findAll()).thenReturn(List.of(oldSlot, newSlot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(false);

        BookingStatus result = appointmentBookingService.modifyAppointment(APPOINTMENT_ID_MODIFY_FAIL, ELEVEN_AM);

        assertEquals(BookingStatus.UPDATE_FAILED, result);
        assertTrue(mockNotificationService.getSentMessages().isEmpty());
        assertTrue(oldSlot.isBooked());
        assertFalse(newSlot.isBooked());
    }

    /**
     * Verifies marking attended triggers an attended notification.
     */
    @Test
    void markAppointmentAsAttended_Success_SendsAttendedNotification() {
        Appointment appointment = new Appointment(
                APPOINTMENT_ID_ATTEND,
                ALICE_EMAIL,
                LocalDate.now().plusDays(1).atTime(LocalTime.parse(TEN_AM)),
                60,
                1,
                AppointmentStatus.CONFIRMED
        );

        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);
        when(appointmentBookingRepository.findById(eq(APPOINTMENT_ID_ATTEND))).thenReturn(Optional.of(appointment));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        BookingStatus result = appointmentBookingService.markAppointmentAsAttended(APPOINTMENT_ID_ATTEND);

        assertEquals(BookingStatus.SUCCESS, result);
        assertEquals(1, mockNotificationService.getSentMessages().size());
        String message = mockNotificationService.getSentMessages().get(0);
        assertTrue(message.contains("Appointment Marked as Attended"));
        assertTrue(message.contains("Booked for:"));
    }

    /**
     * Verifies marking completed triggers a completed notification.
     */
    @Test
    void markAppointmentAsCompleted_Success_SendsCompletedNotification() {
        Appointment appointment = new Appointment(
                APPOINTMENT_ID_COMPLETE,
                ALICE_EMAIL,
                LocalDate.now().plusDays(1).atTime(LocalTime.parse(TEN_AM)),
                60,
                1,
                AppointmentStatus.ATTENDED
        );

        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);
        when(appointmentBookingRepository.findById(eq(APPOINTMENT_ID_COMPLETE))).thenReturn(Optional.of(appointment));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        BookingStatus result = appointmentBookingService.markAppointmentAsCompleted(APPOINTMENT_ID_COMPLETE);

        assertEquals(BookingStatus.SUCCESS, result);
        assertEquals(1, mockNotificationService.getSentMessages().size());
        String message = mockNotificationService.getSentMessages().get(0);
        assertTrue(message.contains("Appointment Completed"));
        assertTrue(message.contains("Booked for:"));
    }

    /**
     * Verifies marking not attended triggers a missed-appointment notification.
     */
    @Test
    void markAppointmentAsNotAttended_Success_SendsNotAttendedNotification() {
        Appointment appointment = new Appointment(
                APPOINTMENT_ID_NOT_ATTENDED,
                ALICE_EMAIL,
                LocalDate.now().plusDays(1).atTime(LocalTime.parse(TEN_AM)),
                60,
                1,
                AppointmentStatus.CONFIRMED
        );

        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);
        when(appointmentBookingRepository.findById(eq(APPOINTMENT_ID_NOT_ATTENDED))).thenReturn(Optional.of(appointment));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        BookingStatus result = appointmentBookingService.markAppointmentAsNotAttended(APPOINTMENT_ID_NOT_ATTENDED);

        assertEquals(BookingStatus.SUCCESS, result);
        assertEquals(1, mockNotificationService.getSentMessages().size());
        String message = mockNotificationService.getSentMessages().get(0);
        assertTrue(message.contains("Appointment Marked as Not Attended"));
        assertTrue(message.contains("Please contact the office if you need to reschedule."));
    }

    private Appointment futureAppointment(String id) {
        return new Appointment(
                id,
                ALICE_EMAIL,
                LocalDate.now().plusDays(1).atTime(LocalTime.parse(TEN_AM)),
                60,
                1,
                AppointmentStatus.CONFIRMED
        );
    }
}

