package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentStatus;
import org.example.domain.UserRole;
import org.example.repository.AppointmentBookingRepository;
import org.example.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for UserAppointmentService covering user-facing appointment operations.
 */
@ExtendWith(MockitoExtension.class)
class UserAppointmentServiceTest {

    @Mock
    private AppointmentBookingRepository appointmentBookingRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private EventManager eventManager;

    @Mock
    private TimeProvider timeProvider;

    private UserAppointmentService userAppointmentService;
    private LocalDateTime now;

    @BeforeEach
    void setup() {
        now = LocalDateTime.of(2026, 3, 31, 10, 0);
        when(timeProvider.now()).thenReturn(now);
        userAppointmentService = new UserAppointmentService(
                appointmentBookingRepository,
                appointmentRepository,
                sessionManager,
                eventManager,
                timeProvider
        );
    }

    @Test
    void testGetMyAppointmentsRequiresLogin() {
        when(sessionManager.isLoggedIn()).thenReturn(false);

        assertThrows(IllegalStateException.class, userAppointmentService::getMyAppointments);
    }

    @Test
    void testGetMyAppointmentsAdminNotAllowed() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);

        assertThrows(IllegalStateException.class, userAppointmentService::getMyAppointments);
    }

    @Test
    void testGetMyAppointmentsForUser() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(false);
        when(sessionManager.getCurrentUsername()).thenReturn("john");

        Appointment appt = new Appointment("john", "10:00", 60, 1, AppointmentStatus.CONFIRMED);
        when(appointmentBookingRepository.findByCustomerName("john")).thenReturn(List.of(appt));

        List<Appointment> appointments = userAppointmentService.getMyAppointments();

        assertEquals(1, appointments.size());
        assertEquals("john", appointments.get(0).getCustomerName());
        verify(appointmentBookingRepository).findByCustomerName("john");
    }

    @Test
    void testModifyAppointmentRequiresLogin() {
        when(sessionManager.isLoggedIn()).thenReturn(false);

        assertThrows(IllegalStateException.class, 
            () -> userAppointmentService.modifyAppointment("id1", now.plusHours(1), 90));
    }

    @Test
    void testModifyAppointmentCannotModifyPastAppointment() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(false);
        when(sessionManager.getCurrentUsername()).thenReturn("john");

        LocalDateTime pastTime = now.minusHours(1);
        Appointment pastAppt = new Appointment("appt1", "john", pastTime, 60, 1, AppointmentStatus.CONFIRMED);

        when(appointmentBookingRepository.findById("appt1")).thenReturn(Optional.of(pastAppt));

        assertThrows(IllegalArgumentException.class, 
            () -> userAppointmentService.modifyAppointment("appt1", now.plusHours(1), 90));
    }

    @Test
    void testModifyFutureAppointment() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(false);
        when(sessionManager.getCurrentUsername()).thenReturn("john");

        LocalDateTime futureTime = now.plusHours(2);
        Appointment futureAppt = new Appointment("appt1", "john", futureTime, 60, 1, AppointmentStatus.CONFIRMED);

        when(appointmentBookingRepository.findById("appt1")).thenReturn(Optional.of(futureAppt));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        LocalDateTime newTime = now.plusHours(3);
        boolean result = userAppointmentService.modifyAppointment("appt1", newTime, 90);

        assertTrue(result);
        verify(appointmentBookingRepository).update(any(Appointment.class));
        verify(eventManager).notifyObservers(contains("modified"));
    }

    @Test
    void testCancelAppointmentCannotCancelPastAppointment() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(false);
        when(sessionManager.getCurrentUsername()).thenReturn("john");

        LocalDateTime pastTime = now.minusHours(1);
        Appointment pastAppt = new Appointment("appt1", "john", pastTime, 60, 1, AppointmentStatus.CONFIRMED);

        when(appointmentBookingRepository.findById("appt1")).thenReturn(Optional.of(pastAppt));

        assertThrows(IllegalArgumentException.class, 
            () -> userAppointmentService.cancelAppointment("appt1"));
    }

    @Test
    void testCancelFutureAppointment() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(false);
        when(sessionManager.getCurrentUsername()).thenReturn("john");

        LocalDateTime futureTime = now.plusHours(2);
        Appointment futureAppt = new Appointment("appt1", "john", futureTime, 60, 1, AppointmentStatus.CONFIRMED);

        when(appointmentBookingRepository.findById("appt1")).thenReturn(Optional.of(futureAppt));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        boolean result = userAppointmentService.cancelAppointment("appt1");

        assertTrue(result);
        verify(appointmentBookingRepository).update(any(Appointment.class));
        verify(eventManager).notifyObservers(contains("cancelled"));
    }

    @Test
    void testGetMyFutureAppointments() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(false);
        when(sessionManager.getCurrentUsername()).thenReturn("john");

        Appointment pastAppt = new Appointment("appt1", "john", now.minusHours(1), 60, 1, AppointmentStatus.CONFIRMED);
        Appointment futureAppt = new Appointment("appt2", "john", now.plusHours(2), 60, 1, AppointmentStatus.CONFIRMED);

        when(appointmentBookingRepository.findByCustomerName("john")).thenReturn(List.of(pastAppt, futureAppt));

        List<Appointment> future = userAppointmentService.getMyFutureAppointments();

        assertEquals(1, future.size());
        assertEquals("appt2", future.get(0).getId());
    }

    @Test
    void testGetMyPastAppointments() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(false);
        when(sessionManager.getCurrentUsername()).thenReturn("john");

        Appointment pastAppt = new Appointment("appt1", "john", now.minusHours(1), 60, 1, AppointmentStatus.CONFIRMED);
        Appointment futureAppt = new Appointment("appt2", "john", now.plusHours(2), 60, 1, AppointmentStatus.CONFIRMED);

        when(appointmentBookingRepository.findByCustomerName("john")).thenReturn(List.of(pastAppt, futureAppt));

        List<Appointment> past = userAppointmentService.getMyPastAppointments();

        assertEquals(1, past.size());
        assertEquals("appt1", past.get(0).getId());
    }
}

