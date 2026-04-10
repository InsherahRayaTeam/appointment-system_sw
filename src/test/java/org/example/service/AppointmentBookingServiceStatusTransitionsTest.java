package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentStatus;
import org.example.repository.AppointmentBookingRepository;
import org.example.repository.AppointmentRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Gap analysis before implementation:
 * - `approveAppointment` had no direct branch-focused unit tests in the service package.
 * - status-transition guards for attended/completed/not-attended had partial happy-path coverage only.
 * - update-failure branches needed stronger assertions for event/notification side effects.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentBookingServiceStatusTransitionsTest {

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

    @Mock
    private AppointmentNotificationCoordinator appointmentNotificationCoordinator;

    private AppointmentBookingService appointmentBookingService;

    @BeforeEach
    void setUp() {
        appointmentBookingService = new AppointmentBookingService(
                appointmentRepository,
                appointmentBookingRepository,
                sessionManager,
                userRepository,
                eventManager,
                appointmentNotificationCoordinator
        );
    }

    @Test
    void approveAppointment_UnauthenticatedUser_ExpectedUnauthorized() {
        when(sessionManager.isLoggedIn()).thenReturn(false);

        BookingStatus result = appointmentBookingService.approveAppointment("apt-1");

        assertEquals(BookingStatus.UNAUTHORIZED, result);
        verifyNoInteractions(appointmentNotificationCoordinator);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
    }

    @Test
    void approveAppointment_MissingAppointment_ExpectedAppointmentNotFound() {
        authenticateAsAdmin();
        when(appointmentBookingRepository.findById("missing")).thenReturn(Optional.empty());

        BookingStatus result = appointmentBookingService.approveAppointment("missing");

        assertEquals(BookingStatus.APPOINTMENT_NOT_FOUND, result);
        verifyNoInteractions(appointmentNotificationCoordinator);
    }

    @Test
    void approveAppointment_CancelledAppointment_ExpectedAlreadyCancelled() {
        authenticateAsAdmin();
        when(appointmentBookingRepository.findById("apt-cancelled"))
                .thenReturn(Optional.of(appointment("apt-cancelled", AppointmentStatus.CANCELLED, 2)));

        BookingStatus result = appointmentBookingService.approveAppointment("apt-cancelled");

        assertEquals(BookingStatus.APPOINTMENT_ALREADY_CANCELLED, result);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
        verifyNoInteractions(appointmentNotificationCoordinator);
    }

    @Test
    void approveAppointment_PastAppointment_ExpectedAppointmentNotFuture() {
        authenticateAsAdmin();
        when(appointmentBookingRepository.findById("apt-past"))
                .thenReturn(Optional.of(appointment("apt-past", AppointmentStatus.CONFIRMED, -1)));

        BookingStatus result = appointmentBookingService.approveAppointment("apt-past");

        assertEquals(BookingStatus.APPOINTMENT_NOT_FUTURE, result);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
        verifyNoInteractions(appointmentNotificationCoordinator);
    }

    @Test
    void approveAppointment_UpdateFails_ExpectedUpdateFailedAndNoNotification() {
        authenticateAsAdmin();
        when(appointmentBookingRepository.findById("apt-update-fail"))
                .thenReturn(Optional.of(appointment("apt-update-fail", AppointmentStatus.CONFIRMED, 2)));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(false);

        BookingStatus result = appointmentBookingService.approveAppointment("apt-update-fail");

        assertEquals(BookingStatus.UPDATE_FAILED, result);
        verify(eventManager, never()).notifyObservers(contains("Reservation approved"));
        verifyNoInteractions(appointmentNotificationCoordinator);
    }

    @Test
    void approveAppointment_ValidFutureReservation_ExpectedSuccessAndApprovedNotification() {
        authenticateAsAdmin();
        when(appointmentBookingRepository.findById("apt-success"))
                .thenReturn(Optional.of(appointment("apt-success", AppointmentStatus.CONFIRMED, 3)));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        BookingStatus result = appointmentBookingService.approveAppointment("apt-success");

        assertEquals(BookingStatus.SUCCESS, result);
        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentBookingRepository).update(captor.capture());
        assertEquals(AppointmentStatus.CONFIRMED, captor.getValue().getStatus());
        verify(eventManager).notifyObservers("Reservation approved: apt-success");
        verify(appointmentNotificationCoordinator).sendApprovedNotification(captor.getValue());
    }

    @Test
    void markAppointmentAsAttended_CompletedAppointment_ExpectedAlreadyCompleted() {
        authenticateAsAdmin();
        when(appointmentBookingRepository.findById("apt-attended-completed"))
                .thenReturn(Optional.of(appointment("apt-attended-completed", AppointmentStatus.COMPLETED, 2)));

        BookingStatus result = appointmentBookingService.markAppointmentAsAttended("apt-attended-completed");

        assertEquals(BookingStatus.APPOINTMENT_ALREADY_COMPLETED, result);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
    }

    @Test
    void markAppointmentAsAttended_NotAttendedAppointment_ExpectedAlreadyNotAttended() {
        authenticateAsAdmin();
        when(appointmentBookingRepository.findById("apt-attended-not-attended"))
                .thenReturn(Optional.of(appointment("apt-attended-not-attended", AppointmentStatus.NOT_ATTENDED, 2)));

        BookingStatus result = appointmentBookingService.markAppointmentAsAttended("apt-attended-not-attended");

        assertEquals(BookingStatus.APPOINTMENT_ALREADY_NOT_ATTENDED, result);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
    }

    @Test
    void markAppointmentAsCompleted_ConfirmedAppointment_ExpectedAppointmentNotAttended() {
        authenticateAsAdmin();
        when(appointmentBookingRepository.findById("apt-complete-confirmed"))
                .thenReturn(Optional.of(appointment("apt-complete-confirmed", AppointmentStatus.CONFIRMED, 2)));

        BookingStatus result = appointmentBookingService.markAppointmentAsCompleted("apt-complete-confirmed");

        assertEquals(BookingStatus.APPOINTMENT_NOT_ATTENDED, result);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
    }

    @Test
    void markAppointmentAsCompleted_UpdateFails_ExpectedUpdateFailedAndNoNotification() {
        authenticateAsAdmin();
        when(appointmentBookingRepository.findById("apt-complete-fail"))
                .thenReturn(Optional.of(appointment("apt-complete-fail", AppointmentStatus.ATTENDED, 2)));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(false);

        BookingStatus result = appointmentBookingService.markAppointmentAsCompleted("apt-complete-fail");

        assertEquals(BookingStatus.UPDATE_FAILED, result);
        verify(eventManager, never()).notifyObservers(contains("Reservation completed"));
        verify(appointmentNotificationCoordinator, never()).sendCompletedNotification(any(Appointment.class));
    }

    @Test
    void markAppointmentAsNotAttended_AttendedAppointment_ExpectedAlreadyAttended() {
        authenticateAsAdmin();
        when(appointmentBookingRepository.findById("apt-not-attended-attended"))
                .thenReturn(Optional.of(appointment("apt-not-attended-attended", AppointmentStatus.ATTENDED, 2)));

        BookingStatus result = appointmentBookingService.markAppointmentAsNotAttended("apt-not-attended-attended");

        assertEquals(BookingStatus.APPOINTMENT_ALREADY_ATTENDED, result);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
    }

    @Test
    void markAppointmentAsNotAttended_UpdateFails_ExpectedUpdateFailedAndNoNotification() {
        authenticateAsAdmin();
        when(appointmentBookingRepository.findById("apt-not-attended-fail"))
                .thenReturn(Optional.of(appointment("apt-not-attended-fail", AppointmentStatus.CONFIRMED, 2)));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(false);

        BookingStatus result = appointmentBookingService.markAppointmentAsNotAttended("apt-not-attended-fail");

        assertEquals(BookingStatus.UPDATE_FAILED, result);
        verify(eventManager, never()).notifyObservers(contains("marked as not attended"));
        verify(appointmentNotificationCoordinator, never()).sendNotAttendedNotification(any(Appointment.class));
    }

    private void authenticateAsAdmin() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);
    }

    private Appointment appointment(String id, AppointmentStatus status, int dayOffset) {
        return new Appointment(
                id,
                "customer@example.com",
                LocalDateTime.now().plusDays(dayOffset),
                60,
                1,
                status
        );
    }
}
