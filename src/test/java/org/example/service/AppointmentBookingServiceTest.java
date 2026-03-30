package org.example.service;

import org.example.domain.AdminUser;
import org.example.domain.Appointment;
import org.example.domain.AppointmentSlot;
import org.example.domain.AppointmentStatus;
import org.example.domain.UserRole;
import org.example.repository.AdminRepository;
import org.example.repository.AppointmentBookingRepository;
import org.example.repository.AppointmentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

class AppointmentBookingServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentBookingRepository appointmentBookingRepository;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private EventManager eventManager;

    private AppointmentBookingService appointmentBookingService;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        appointmentBookingService = new AppointmentBookingService(
                appointmentRepository,
                appointmentBookingRepository,
                sessionManager,
                adminRepository,
                eventManager
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void bookAppointment_ValidRequest_ReturnsSuccessAndSavesAppointment() {
        AppointmentSlot slot = new AppointmentSlot("10:00");
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        BookingStatus result = appointmentBookingService.bookAppointment("Alice", "10:00", 60, 3);

        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);

        assertEquals(BookingStatus.SUCCESS, result);
        assertFalse(slot.isAvailable());
        verify(appointmentBookingRepository).save(appointmentCaptor.capture());

        Appointment appointment = appointmentCaptor.getValue();
        assertEquals("Alice", appointment.getCustomerName());
        assertEquals("10:00", appointment.getSlotTime());
        assertEquals(60, appointment.getDurationMinutes());
        assertEquals(3, appointment.getParticipantCount());
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
    }

    @Test
    void cancelAppointment_FutureReservation_ReturnsSuccess() {
        authenticateAsAdmin();

        AppointmentSlot slot = new AppointmentSlot("10:00");
        slot.book();
        Appointment appointment = futureReservation("apt-1", "10:00");

        when(appointmentBookingRepository.findById(eq("apt-1"))).thenReturn(Optional.of(appointment));
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        BookingStatus result = appointmentBookingService.cancelAppointment("apt-1");

        assertEquals(BookingStatus.SUCCESS, result);
        assertTrue(slot.isAvailable());

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentBookingRepository).update(captor.capture());
        assertEquals(AppointmentStatus.CANCELLED, captor.getValue().getStatus());
    }

    @Test
    void cancelAppointment_PastReservation_ReturnsAppointmentNotFuture() {
        authenticateAsAdmin();

        AppointmentSlot slot = new AppointmentSlot("10:00");
        slot.book();

        when(appointmentBookingRepository.findById(eq("apt-2"))).thenReturn(Optional.of(pastReservation("apt-2")));

        BookingStatus result = appointmentBookingService.cancelAppointment("apt-2");

        assertEquals(BookingStatus.APPOINTMENT_NOT_FUTURE, result);
        assertFalse(slot.isAvailable());
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
    }

    @Test
    void cancelAppointment_ReleasesSlot() {
        authenticateAsAdmin();

        AppointmentSlot slot = new AppointmentSlot("11:00");
        slot.book();

        when(appointmentBookingRepository.findById(eq("apt-3"))).thenReturn(Optional.of(
                futureReservation("apt-3", "11:00")
        ));
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        BookingStatus result = appointmentBookingService.cancelAppointment("apt-3");

        assertEquals(BookingStatus.SUCCESS, result);
        assertTrue(slot.isAvailable());
    }

    @Test
    void modifyAppointment_FutureReservationToAnotherAvailableSlot_ReturnsSuccess() {
        authenticateAsAdmin();

        AppointmentSlot oldSlot = new AppointmentSlot("10:00");
        oldSlot.book();
        AppointmentSlot newSlot = new AppointmentSlot("11:00");

        when(appointmentBookingRepository.findById(eq("apt-4"))).thenReturn(Optional.of(futureReservation("apt-4", "10:00")));
        when(appointmentRepository.findAll()).thenReturn(List.of(oldSlot, newSlot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        BookingStatus result = appointmentBookingService.modifyAppointment("apt-4", "11:00");

        assertEquals(BookingStatus.SUCCESS, result);
        assertTrue(oldSlot.isAvailable());
        assertFalse(newSlot.isAvailable());

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentBookingRepository).update(captor.capture());
        assertEquals("11:00", captor.getValue().getSlotTime());
        assertEquals(AppointmentStatus.MODIFIED, captor.getValue().getStatus());
    }

    @Test
    void modifyAppointment_PastReservation_ReturnsAppointmentNotFuture() {
        authenticateAsAdmin();

        when(appointmentBookingRepository.findById(eq("apt-5"))).thenReturn(Optional.of(pastReservation("apt-5")));

        BookingStatus result = appointmentBookingService.modifyAppointment("apt-5", "11:00");

        assertEquals(BookingStatus.APPOINTMENT_NOT_FUTURE, result);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
    }

    @Test
    void modifyAppointment_ToUnavailableSlot_ReturnsSlotAlreadyBooked() {
        authenticateAsAdmin();

        AppointmentSlot oldSlot = new AppointmentSlot("10:00");
        oldSlot.book();
        AppointmentSlot targetSlot = new AppointmentSlot("11:00");
        targetSlot.book();

        when(appointmentBookingRepository.findById(eq("apt-6"))).thenReturn(Optional.of(futureReservation("apt-6", "10:00")));
        when(appointmentRepository.findAll()).thenReturn(List.of(oldSlot, targetSlot));

        BookingStatus result = appointmentBookingService.modifyAppointment("apt-6", "11:00");

        assertEquals(BookingStatus.SLOT_ALREADY_BOOKED, result);
        assertFalse(oldSlot.isAvailable());
        assertFalse(targetSlot.isAvailable());
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
    }

    @Test
    void adminCanManageReservations() {
        authenticateAsAdmin();

        Appointment reservation = futureReservation("apt-7", "10:00");
        when(appointmentBookingRepository.findAll()).thenReturn(List.of(reservation));

        assertTrue(appointmentBookingService.canCurrentUserManageReservations());
        List<Appointment> reservations = appointmentBookingService.getManagedReservations();

        assertEquals(1, reservations.size());
        assertEquals("apt-7", reservations.get(0).getId());
    }

    @Test
    void nonAuthenticatedOrNonAdminAccessIsRejected() {
        when(sessionManager.isLoggedIn()).thenReturn(false);
        when(sessionManager.isAdmin()).thenReturn(false);

        BookingStatus loggedOutStatus = appointmentBookingService.cancelAppointment("apt-8");
        assertEquals(BookingStatus.UNAUTHORIZED, loggedOutStatus);

        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(false);
        when(sessionManager.getCurrentUsername()).thenReturn("operator");
        when(adminRepository.findByUsername("operator")).thenReturn(Optional.empty());

        BookingStatus nonAdminStatus = appointmentBookingService.modifyAppointment("apt-8", "11:00");
        assertEquals(BookingStatus.UNAUTHORIZED, nonAdminStatus);
        assertTrue(appointmentBookingService.getManagedReservations().isEmpty());
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void authenticatedRegularUser_CannotManageReservations() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(false);
        when(sessionManager.getCurrentUsername()).thenReturn("user");
        when(adminRepository.findByUsername("user")).thenReturn(
                Optional.of(new AdminUser("user-1", "user", "user123", UserRole.USER))
        );

        BookingStatus status = appointmentBookingService.cancelAppointment("apt-9");

        assertEquals(BookingStatus.UNAUTHORIZED, status);
        assertTrue(appointmentBookingService.getManagedReservations().isEmpty());
    }

    private void authenticateAsAdmin() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);
        when(sessionManager.getCurrentUsername()).thenReturn("admin");
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(new AdminUser("admin", "admin")));
    }

    private Appointment futureReservation(String id, String slotTime) {
        return new Appointment(
                id,
                "Alice",
                LocalDate.now().plusDays(1).atTime(LocalTime.parse(slotTime)),
                60,
                2,
                AppointmentStatus.CONFIRMED
        );
    }

    private Appointment pastReservation(String id) {
        return new Appointment(
                id,
                "Alice",
                LocalDate.now().minusDays(1).atTime(LocalTime.of(10, 0)),
                60,
                2,
                AppointmentStatus.CONFIRMED
        );
    }
}
