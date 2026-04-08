package org.example.presentation.gui;

import org.example.domain.Appointment;
import org.example.domain.AppointmentSlot;
import org.example.domain.AppointmentStatus;
import org.example.domain.AppointmentType;
import org.example.repository.AppointmentBookingRepository;
import org.example.repository.AppointmentRepository;
import org.example.repository.UserRepository;
import org.example.service.BookingStatus;
import org.example.service.EventManager;
import org.example.service.SessionManager;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentBookingServiceTest {

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

    private AppointmentBookingService appointmentBookingService;

    @BeforeEach
    void setUp() {
        appointmentBookingService = new AppointmentBookingService(
                appointmentRepository,
                appointmentBookingRepository,
                sessionManager,
                userRepository,
                eventManager
        );
    }

    @Test
    void bookAppointment_ValidNormalType_ReturnsSuccessAndBooksSlot() {
        AppointmentSlot slot = new AppointmentSlot("10:00");
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        BookingStatus result = appointmentBookingService.bookAppointment(
                "alice@example.com",
                "10:00",
                60,
                2,
                null
        );

        assertEquals(BookingStatus.SUCCESS, result);
        assertFalse(slot.isAvailable());
        verify(appointmentBookingRepository).save(any(Appointment.class));
    }

    @Test
    void bookAppointment_UrgentTypeTooLong_ReturnsInvalidAppointmentRules() {
        BookingStatus result = appointmentBookingService.bookAppointment(
                "alice@example.com",
                "10:00",
                60,
                1,
                AppointmentType.URGENT
        );

        assertEquals(BookingStatus.INVALID_APPOINTMENT_RULES, result);
        verifyNoInteractions(appointmentRepository);
        verifyNoInteractions(appointmentBookingRepository);
    }

    @Test
    void cancelAppointment_WhenNotLoggedIn_ReturnsUnauthorized() {
        when(sessionManager.isLoggedIn()).thenReturn(false);

        BookingStatus result = appointmentBookingService.cancelAppointment("apt-1");

        assertEquals(BookingStatus.UNAUTHORIZED, result);
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void cancelAppointment_WhenMissingReservation_ReturnsAppointmentNotFound() {
        authenticateAsAdmin();
        when(appointmentBookingRepository.findById(eq("missing-id"))).thenReturn(Optional.empty());

        BookingStatus result = appointmentBookingService.cancelAppointment("missing-id");

        assertEquals(BookingStatus.APPOINTMENT_NOT_FOUND, result);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
    }

    @Test
    void cancelAppointment_WhenAlreadyCancelled_ReturnsAlreadyCancelled() {
        authenticateAsAdmin();
        Appointment cancelled = new Appointment(
                "apt-cancelled",
                "alice@example.com",
                LocalDate.now().plusDays(1).atTime(LocalTime.parse("10:00")),
                60,
                2,
                AppointmentStatus.CANCELLED
        );
        when(appointmentBookingRepository.findById(eq("apt-cancelled"))).thenReturn(Optional.of(cancelled));

        BookingStatus result = appointmentBookingService.cancelAppointment("apt-cancelled");

        assertEquals(BookingStatus.APPOINTMENT_ALREADY_CANCELLED, result);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
    }

    @Test
    void modifyAppointment_TargetSlotMissing_ReturnsSlotNotFound() {
        authenticateAsAdmin();
        AppointmentSlot oldSlot = new AppointmentSlot("10:00");
        oldSlot.book();

        when(appointmentBookingRepository.findById(eq("apt-2")))
                .thenReturn(Optional.of(new Appointment(
                        "apt-2",
                        "alice@example.com",
                        LocalDate.now().plusDays(1).atTime(LocalTime.parse("10:00")),
                        60,
                        2,
                        AppointmentStatus.CONFIRMED
                )));
        when(appointmentRepository.findAll()).thenReturn(List.of(oldSlot));

        BookingStatus result = appointmentBookingService.modifyAppointment("apt-2", "11:00");

        assertEquals(BookingStatus.SLOT_NOT_FOUND, result);
        assertFalse(oldSlot.isAvailable());
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
        assertTrue(oldSlot.isBooked());
    }

    private void authenticateAsAdmin() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);
    }
}

