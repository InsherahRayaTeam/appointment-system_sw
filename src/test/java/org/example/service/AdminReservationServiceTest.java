package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentStatus;
import org.example.domain.UserRole;
import org.example.repository.AppointmentBookingRepository;
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
 * Tests for AdminReservationService covering admin-only reservation management.
 */
@ExtendWith(MockitoExtension.class)
class AdminReservationServiceTest {

    @Mock
    private AppointmentBookingRepository appointmentBookingRepository;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private EventManager eventManager;

    private AdminReservationService adminReservationService;

    @BeforeEach
    void setup() {
        adminReservationService = new AdminReservationService(
                appointmentBookingRepository,
                sessionManager,
                eventManager
        );
    }

    @Test
    void testGetAllReservationsRequiresLogin() {
        when(sessionManager.isLoggedIn()).thenReturn(false);

        assertThrows(IllegalStateException.class, adminReservationService::getAllReservations);
    }

    @Test
    void testGetAllReservationsRequiresAdminRole() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(false);

        assertThrows(IllegalStateException.class, adminReservationService::getAllReservations);
    }

    @Test
    void testGetAllReservationsWhenAdmin() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);

        Appointment appt1 = new Appointment("john", "10:00", 60, 1, AppointmentStatus.CONFIRMED);
        Appointment appt2 = new Appointment("jane", "11:00", 60, 1, AppointmentStatus.CONFIRMED);

        when(appointmentBookingRepository.findAll()).thenReturn(List.of(appt1, appt2));

        List<Appointment> reservations = adminReservationService.getAllReservations();

        assertEquals(2, reservations.size());
        verify(appointmentBookingRepository).findAll();
    }

    @Test
    void testGetReservationsByCustomerRequiresAdmin() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> adminReservationService.getReservationsByCustomer("john"));
    }

    @Test
    void testGetReservationsByCustomerWhenAdmin() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);

        Appointment appt = new Appointment("john", "10:00", 60, 1, AppointmentStatus.CONFIRMED);
        when(appointmentBookingRepository.findByCustomerName("john")).thenReturn(List.of(appt));

        List<Appointment> reservations = adminReservationService.getReservationsByCustomer("john");

        assertEquals(1, reservations.size());
        assertEquals("john", reservations.get(0).getCustomerName());
        verify(appointmentBookingRepository).findByCustomerName("john");
    }

    @Test
    void testGetReservationByIdRequiresAdmin() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> adminReservationService.getReservation("appt1"));
    }

    @Test
    void testGetReservationByIdWhenAdmin() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);

        Appointment appt = new Appointment("john", "10:00", 60, 1, AppointmentStatus.CONFIRMED);
        when(appointmentBookingRepository.findById("appt1")).thenReturn(Optional.of(appt));

        Optional<Appointment> result = adminReservationService.getReservation("appt1");

        assertTrue(result.isPresent());
        assertEquals("john", result.get().getCustomerName());
    }

    @Test
    void testCancelReservationRequiresAdmin() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(false);

        assertThrows(IllegalStateException.class, 
            () -> adminReservationService.cancelReservation("appt1", "no reason"));
    }

    @Test
    void testCancelReservationNotFound() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);
        when(appointmentBookingRepository.findById("appt1")).thenReturn(Optional.empty());

        boolean result = adminReservationService.cancelReservation("appt1", "not found");

        assertFalse(result);
    }

    @Test
    void testCancelReservationSuccess() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);
        when(sessionManager.getCurrentUsername()).thenReturn("admin");

        Appointment appt = new Appointment("john", "10:00", 60, 1, AppointmentStatus.CONFIRMED);
        when(appointmentBookingRepository.findById("appt1")).thenReturn(Optional.of(appt));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        boolean result = adminReservationService.cancelReservation("appt1", "double booking");

        assertTrue(result);
        verify(appointmentBookingRepository).update(any(Appointment.class));
        verify(eventManager).notifyObservers(contains("cancelled by ADMIN"));
    }

    @Test
    void testGetReservationStatsRequiresAdmin() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(false);

        assertThrows(IllegalStateException.class, adminReservationService::getReservationStats);
    }

    @Test
    void testGetReservationStatsWhenAdmin() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);

        Appointment active1 = new Appointment("john", "10:00", 60, 1, AppointmentStatus.CONFIRMED);
        Appointment active2 = new Appointment("jane", "11:00", 60, 1, AppointmentStatus.CONFIRMED);
        Appointment cancelled = new Appointment("bob", "12:00", 60, 1, AppointmentStatus.CANCELLED);

        when(appointmentBookingRepository.findAll()).thenReturn(List.of(active1, active2, cancelled));

        AdminReservationService.ReservationStats stats = adminReservationService.getReservationStats();

        assertEquals(3, stats.getTotalReservations());
        assertEquals(2, stats.getActiveReservations());
        assertEquals(1, stats.getCancelledReservations());
    }
}

