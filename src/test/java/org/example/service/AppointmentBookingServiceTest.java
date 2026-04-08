package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentSlot;
import org.example.domain.AppointmentStatus;
import org.example.domain.AppointmentType;
import org.example.domain.SystemUser;
import org.example.domain.UserRole;
import org.example.repository.AppointmentBookingRepository;
import org.example.repository.AppointmentRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    void bookAppointment_ValidRequest_ReturnsSuccessAndSavesAppointment() {
        // Arrange
        AppointmentSlot slot = new AppointmentSlot("10:00");
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        // Act
        BookingStatus result = appointmentBookingService.bookAppointment("alice@example.com", "10:00", 60, 3);

        // Assert
        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
        assertEquals(BookingStatus.SUCCESS, result);
        assertFalse(slot.isAvailable());
        verify(appointmentBookingRepository).save(appointmentCaptor.capture());

        Appointment appointment = appointmentCaptor.getValue();
        assertEquals("alice@example.com", appointment.getCustomerName());
        assertEquals("10:00", appointment.getSlotTime());
        assertEquals(60, appointment.getDurationMinutes());
        assertEquals(3, appointment.getParticipantCount());
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
    }

    @Test
    void bookAppointment_WithStringInputs_InvalidDuration_ReturnsInvalidDuration() {
        // Arrange / Act
        BookingStatus status = appointmentBookingService.bookAppointment("alice@example.com", "10:00", "bad", "2");

        // Assert
        assertEquals(BookingStatus.INVALID_DURATION, status);
        verifyNoInteractions(appointmentRepository);
        verifyNoInteractions(appointmentBookingRepository);
    }

    @Test
    void bookAppointment_WithStringInputs_InvalidParticipantCount_ReturnsInvalidParticipantCount() {
        // Arrange / Act
        BookingStatus status = appointmentBookingService.bookAppointment("alice@example.com", "10:00", "60", "bad");

        // Assert
        assertEquals(BookingStatus.INVALID_PARTICIPANT_COUNT, status);
        verifyNoInteractions(appointmentRepository);
        verifyNoInteractions(appointmentBookingRepository);
    }

    @Test
    void bookAppointment_BlankCustomer_ReturnsBlankCustomerName() {
        // Arrange / Act
        BookingStatus status = appointmentBookingService.bookAppointment("   ", "10:00", 60, 2);

        // Assert
        assertEquals(BookingStatus.BLANK_CUSTOMER_NAME, status);
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void bookAppointment_BlankSlot_ReturnsBlankSlotTime() {
        // Arrange / Act
        BookingStatus status = appointmentBookingService.bookAppointment("alice@example.com", "   ", 60, 2);

        // Assert
        assertEquals(BookingStatus.BLANK_SLOT_TIME, status);
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void bookAppointment_UnavailableSlot_ReturnsSlotAlreadyBooked() {
        // Arrange
        AppointmentSlot slot = new AppointmentSlot("10:00");
        slot.book();
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        // Act
        BookingStatus result = appointmentBookingService.bookAppointment("alice@example.com", "10:00", 60, 2);

        // Assert
        assertEquals(BookingStatus.SLOT_ALREADY_BOOKED, result);
        verify(appointmentBookingRepository, never()).save(any());
    }

    @Test
    void bookAppointment_MissingSlot_ReturnsSlotNotFound() {
        // Arrange
        when(appointmentRepository.findAll()).thenReturn(List.of(new AppointmentSlot("11:00")));

        // Act
        BookingStatus result = appointmentBookingService.bookAppointment("alice@example.com", "10:00", 60, 2);

        // Assert
        assertEquals(BookingStatus.SLOT_NOT_FOUND, result);
        verify(appointmentBookingRepository, never()).save(any());
    }

    @Test
    void bookAppointment_ZeroParticipants_CurrentlyAcceptedByRuleImplementation() {
        // Arrange
        AppointmentSlot slot = new AppointmentSlot("10:00");
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        // Act
        BookingStatus result = appointmentBookingService.bookAppointment("alice@example.com", "10:00", 60, 0);

        // Assert
        assertEquals(BookingStatus.SUCCESS, result);
    }

    @Test
    void bookAppointment_UrgentTypeWithTooLongDuration_ReturnsInvalidAppointmentRules() {
        BookingStatus result = appointmentBookingService.bookAppointment(
                "alice@example.com",
                "10:00",
                60,
                2,
                AppointmentType.URGENT
        );

        assertEquals(BookingStatus.INVALID_APPOINTMENT_RULES, result);
        verifyNoInteractions(appointmentRepository);
        verifyNoInteractions(appointmentBookingRepository);
    }

    @Test
    void bookAppointment_GroupTypeWithEnoughParticipants_ReturnsSuccessAndSavesGroupType() {
        AppointmentSlot slot = new AppointmentSlot("10:00");
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        BookingStatus result = appointmentBookingService.bookAppointment(
                "alice@example.com",
                "10:00",
                60,
                3,
                AppointmentType.GROUP
        );

        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
        assertEquals(BookingStatus.SUCCESS, result);
        verify(appointmentBookingRepository).save(appointmentCaptor.capture());
        assertEquals(AppointmentType.GROUP, appointmentCaptor.getValue().getType());
    }

    @Test
    void getManagedReservations_WhenAuthenticatedAdmin_ReturnsAllReservations() {
        // Arrange
        authenticateAsAdmin();
        Appointment reservation = futureReservation("apt-1", "10:00", "alice@example.com");
        when(appointmentBookingRepository.findAll()).thenReturn(List.of(reservation));

        // Act
        List<Appointment> reservations = appointmentBookingService.getManagedReservations();

        // Assert
        assertEquals(1, reservations.size());
        assertEquals("apt-1", reservations.get(0).getId());
        assertTrue(appointmentBookingService.canCurrentUserManageReservations());
    }

    @Test
    void canCurrentUserManageReservations_WhenEmailResolvesToAdminRole_ReturnsTrue() {
        // Arrange
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(false);
        when(sessionManager.getCurrentEmail()).thenReturn("admin@gmail.com");
        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(
                Optional.of(new SystemUser("admin-1", "admin@gmail.com", "admin123", UserRole.ADMIN))
        );

        // Act
        boolean canManage = appointmentBookingService.canCurrentUserManageReservations();

        // Assert
        assertTrue(canManage);
    }

    @Test
    void getManagedReservations_WhenNotAdmin_ReturnsEmptyList() {
        // Arrange
        when(sessionManager.isLoggedIn()).thenReturn(false);

        // Act
        List<Appointment> reservations = appointmentBookingService.getManagedReservations();

        // Assert
        assertTrue(reservations.isEmpty());
    }

    @Test
    void getReservationsForCustomer_AsAdmin_ReturnsMatchingReservations() {
        // Arrange
        authenticateAsAdmin();
        Appointment first = futureReservation("apt-10", "10:00", "alice@example.com");
        Appointment second = futureReservation("apt-11", "11:00", "bob@example.com");
        when(appointmentBookingRepository.findAll()).thenReturn(List.of(first, second));

        // Act
        List<Appointment> reservations = appointmentBookingService.getReservationsForCustomer("alice@example.com");

        // Assert
        assertEquals(1, reservations.size());
        assertEquals("apt-10", reservations.get(0).getId());
    }

    @Test
    void getReservationsForCustomer_AsRegularUser_ReturnsOnlyOwnReservations() {
        // Arrange
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(false);
        when(sessionManager.getCurrentEmail()).thenReturn("user@example.com");

        Appointment own = futureReservation("apt-20", "10:00", "user@example.com");
        Appointment other = futureReservation("apt-21", "11:00", "other@example.com");
        when(appointmentBookingRepository.findAll()).thenReturn(List.of(own, other));

        // Act
        List<Appointment> ownReservations = appointmentBookingService.getReservationsForCustomer("user@example.com");
        List<Appointment> otherReservations = appointmentBookingService.getReservationsForCustomer("other@example.com");

        // Assert
        assertEquals(1, ownReservations.size());
        assertEquals("apt-20", ownReservations.get(0).getId());
        assertTrue(otherReservations.isEmpty());
    }

    @Test
    void cancelAppointment_WhenUnauthorized_ReturnsUnauthorized() {
        // Arrange
        when(sessionManager.isLoggedIn()).thenReturn(false);

        // Act
        BookingStatus status = appointmentBookingService.cancelAppointment("apt-30");

        // Assert
        assertEquals(BookingStatus.UNAUTHORIZED, status);
        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void cancelAppointment_FutureReservation_ReturnsSuccessAndReleasesSlot() {
        // Arrange
        authenticateAsAdmin();

        AppointmentSlot slot = new AppointmentSlot("10:00");
        slot.book();
        Appointment appointment = futureReservation("apt-31", "10:00", "alice@example.com");

        when(appointmentBookingRepository.findById(eq("apt-31"))).thenReturn(Optional.of(appointment));
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        // Act
        BookingStatus result = appointmentBookingService.cancelAppointment("apt-31");

        // Assert
        assertEquals(BookingStatus.SUCCESS, result);
        assertTrue(slot.isAvailable());

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentBookingRepository).update(captor.capture());
        assertEquals(AppointmentStatus.CANCELLED, captor.getValue().getStatus());
        verify(eventManager).notifyObservers("Reservation cancelled: apt-31");
    }

    @Test
    void cancelAppointment_PastReservation_ReturnsAppointmentNotFuture() {
        // Arrange
        authenticateAsAdmin();
        when(appointmentBookingRepository.findById(eq("apt-32"))).thenReturn(Optional.of(defaultPastReservation()));

        // Act
        BookingStatus result = appointmentBookingService.cancelAppointment("apt-32");

        // Assert
        assertEquals(BookingStatus.APPOINTMENT_NOT_FUTURE, result);
        verify(appointmentBookingRepository, never()).update(any());
    }

    @Test
    void cancelAppointment_WhenReservationNotFound_ReturnsAppointmentNotFound() {
        // Arrange
        authenticateAsAdmin();
        when(appointmentBookingRepository.findById(eq("missing-id"))).thenReturn(Optional.empty());

        // Act
        BookingStatus result = appointmentBookingService.cancelAppointment("missing-id");

        // Assert
        assertEquals(BookingStatus.APPOINTMENT_NOT_FOUND, result);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
    }

    @Test
    void cancelAppointment_WhenAlreadyCancelled_ReturnsAlreadyCancelled() {
        // Arrange
        authenticateAsAdmin();
        Appointment cancelled = new Appointment(
                "apt-34",
                "alice@example.com",
                LocalDate.now().plusDays(1).atTime(LocalTime.parse("10:00")),
                60,
                2,
                AppointmentStatus.CANCELLED
        );
        when(appointmentBookingRepository.findById(eq("apt-34"))).thenReturn(Optional.of(cancelled));

        // Act
        BookingStatus result = appointmentBookingService.cancelAppointment("apt-34");

        // Assert
        assertEquals(BookingStatus.APPOINTMENT_ALREADY_CANCELLED, result);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
    }

    @Test
    void cancelAppointment_WhenUpdateFails_RestoresReleasedSlot() {
        // Arrange
        authenticateAsAdmin();

        AppointmentSlot slot = new AppointmentSlot("10:00");
        slot.book();
        Appointment appointment = futureReservation("apt-33", "10:00", "alice@example.com");

        when(appointmentBookingRepository.findById(eq("apt-33"))).thenReturn(Optional.of(appointment));
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(false);

        // Act
        BookingStatus result = appointmentBookingService.cancelAppointment("apt-33");

        // Assert
        assertEquals(BookingStatus.UPDATE_FAILED, result);
        assertFalse(slot.isAvailable());
    }

    @Test
    void cancelOwnAppointment_UserOwnFutureReservation_ReturnsSuccessAndReleasesSlot() {
        // Arrange
        authenticateAsUser("alice@example.com");

        AppointmentSlot slot = new AppointmentSlot("10:00");
        slot.book();
        Appointment appointment = futureReservation("apt-user-1", "10:00", "alice@example.com");

        when(appointmentBookingRepository.findById(eq("apt-user-1"))).thenReturn(Optional.of(appointment));
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        // Act
        BookingStatus result = appointmentBookingService.cancelOwnAppointment("apt-user-1");

        // Assert
        assertEquals(BookingStatus.SUCCESS, result);
        assertTrue(slot.isAvailable());
        verify(eventManager).notifyObservers("Reservation cancelled: apt-user-1");
    }

    @Test
    void cancelOwnAppointment_UserOtherReservation_ReturnsUnauthorized() {
        // Arrange
        authenticateAsUser("alice@example.com");
        when(appointmentBookingRepository.findById(eq("apt-user-2")))
                .thenReturn(Optional.of(futureReservation("apt-user-2", "10:00", "bob@example.com")));

        // Act
        BookingStatus result = appointmentBookingService.cancelOwnAppointment("apt-user-2");

        // Assert
        assertEquals(BookingStatus.UNAUTHORIZED, result);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
    }

    @Test
    void cancelOwnAppointment_UserPastReservation_ReturnsAppointmentNotFuture() {
        // Arrange
        authenticateAsUser("alice@example.com");
        Appointment past = new Appointment(
                "apt-user-3",
                "alice@example.com",
                LocalDate.now().minusDays(1).atTime(LocalTime.parse("10:00")),
                60,
                2,
                AppointmentStatus.CONFIRMED
        );
        when(appointmentBookingRepository.findById(eq("apt-user-3"))).thenReturn(Optional.of(past));

        // Act
        BookingStatus result = appointmentBookingService.cancelOwnAppointment("apt-user-3");

        // Assert
        assertEquals(BookingStatus.APPOINTMENT_NOT_FUTURE, result);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
    }

    @Test
    void cancelOwnAppointment_WhenUnauthenticated_ReturnsUnauthorized() {
        // Arrange
        when(sessionManager.isLoggedIn()).thenReturn(false);

        // Act
        BookingStatus result = appointmentBookingService.cancelOwnAppointment("apt-user-4");

        // Assert
        assertEquals(BookingStatus.UNAUTHORIZED, result);
    }

    @Test
    void modifyAppointment_WhenUnauthorized_ReturnsUnauthorized() {
        // Arrange
        when(sessionManager.isLoggedIn()).thenReturn(false);

        // Act
        BookingStatus status = appointmentBookingService.modifyAppointment("apt-40", "11:00");

        // Assert
        assertEquals(BookingStatus.UNAUTHORIZED, status);
    }

    @Test
    void modifyAppointment_BlankNewSlot_ReturnsBlankSlotTime() {
        // Act
        BookingStatus status = appointmentBookingService.modifyAppointment("apt-41", "  ");

        // Assert
        assertEquals(BookingStatus.BLANK_SLOT_TIME, status);
    }

    @Test
    void modifyAppointment_FutureReservationToAvailableSlot_ReturnsSuccess() {
        // Arrange
        authenticateAsAdmin();

        AppointmentSlot oldSlot = new AppointmentSlot("10:00");
        oldSlot.book();
        AppointmentSlot newSlot = new AppointmentSlot("11:00");

        when(appointmentBookingRepository.findById(eq("apt-42")))
                .thenReturn(Optional.of(futureReservation("apt-42", "10:00", "alice@example.com")));
        when(appointmentRepository.findAll()).thenReturn(List.of(oldSlot, newSlot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        // Act
        BookingStatus result = appointmentBookingService.modifyAppointment("apt-42", "11:00");

        // Assert
        assertEquals(BookingStatus.SUCCESS, result);
        assertTrue(oldSlot.isAvailable());
        assertFalse(newSlot.isAvailable());

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentBookingRepository).update(captor.capture());
        assertEquals("11:00", captor.getValue().getSlotTime());
        assertEquals(AppointmentStatus.MODIFIED, captor.getValue().getStatus());
        verify(eventManager).notifyObservers("Reservation modified: apt-42 -> 11:00");
    }

    @Test
    void modifyAppointment_ToUnavailableSlot_ReturnsSlotAlreadyBooked() {
        // Arrange
        authenticateAsAdmin();

        AppointmentSlot oldSlot = new AppointmentSlot("10:00");
        oldSlot.book();
        AppointmentSlot targetSlot = new AppointmentSlot("11:00");
        targetSlot.book();

        when(appointmentBookingRepository.findById(eq("apt-43")))
                .thenReturn(Optional.of(futureReservation("apt-43", "10:00", "alice@example.com")));
        when(appointmentRepository.findAll()).thenReturn(List.of(oldSlot, targetSlot));

        // Act
        BookingStatus result = appointmentBookingService.modifyAppointment("apt-43", "11:00");

        // Assert
        assertEquals(BookingStatus.SLOT_ALREADY_BOOKED, result);
        verify(appointmentBookingRepository, never()).update(any());
        assertFalse(oldSlot.isAvailable());
        assertFalse(targetSlot.isAvailable());
    }

    @Test
    void modifyAppointment_WhenReservationNotFound_ReturnsAppointmentNotFound() {
        // Arrange
        authenticateAsAdmin();
        when(appointmentBookingRepository.findById(eq("apt-404"))).thenReturn(Optional.empty());

        // Act
        BookingStatus result = appointmentBookingService.modifyAppointment("apt-404", "11:00");

        // Assert
        assertEquals(BookingStatus.APPOINTMENT_NOT_FOUND, result);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
    }

    @Test
    void modifyAppointment_WhenAlreadyCancelled_ReturnsAlreadyCancelled() {
        // Arrange
        authenticateAsAdmin();
        Appointment cancelled = new Appointment(
                "apt-45",
                "alice@example.com",
                LocalDate.now().plusDays(1).atTime(LocalTime.parse("10:00")),
                60,
                2,
                AppointmentStatus.CANCELLED
        );
        when(appointmentBookingRepository.findById(eq("apt-45"))).thenReturn(Optional.of(cancelled));

        // Act
        BookingStatus result = appointmentBookingService.modifyAppointment("apt-45", "11:00");

        // Assert
        assertEquals(BookingStatus.APPOINTMENT_ALREADY_CANCELLED, result);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
    }

    @Test
    void modifyAppointment_TargetSlotNotFound_ReturnsSlotNotFound() {
        // Arrange
        authenticateAsAdmin();
        AppointmentSlot oldSlot = new AppointmentSlot("10:00");
        oldSlot.book();

        when(appointmentBookingRepository.findById(eq("apt-46")))
                .thenReturn(Optional.of(futureReservation("apt-46", "10:00", "alice@example.com")));
        when(appointmentRepository.findAll()).thenReturn(List.of(oldSlot));

        // Act
        BookingStatus result = appointmentBookingService.modifyAppointment("apt-46", "11:00");

        // Assert
        assertEquals(BookingStatus.SLOT_NOT_FOUND, result);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
    }

    @Test
    void modifyAppointment_WhenUpdateFails_RollsBackSlotChanges() {
        // Arrange
        authenticateAsAdmin();

        AppointmentSlot oldSlot = new AppointmentSlot("10:00");
        oldSlot.book();
        AppointmentSlot targetSlot = new AppointmentSlot("11:00");

        when(appointmentBookingRepository.findById(eq("apt-44")))
                .thenReturn(Optional.of(futureReservation("apt-44", "10:00", "alice@example.com")));
        when(appointmentRepository.findAll()).thenReturn(List.of(oldSlot, targetSlot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(false);

        // Act
        BookingStatus result = appointmentBookingService.modifyAppointment("apt-44", "11:00");

        // Assert
        assertEquals(BookingStatus.UPDATE_FAILED, result);
        assertFalse(oldSlot.isAvailable());
        assertTrue(targetSlot.isAvailable());
    }

    @Test
    void modifyOwnAppointment_UserOwnFutureReservation_ReturnsSuccess() {
        // Arrange
        authenticateAsUser("alice@example.com");

        AppointmentSlot oldSlot = new AppointmentSlot("10:00");
        oldSlot.book();
        AppointmentSlot newSlot = new AppointmentSlot("11:00");

        when(appointmentBookingRepository.findById(eq("apt-user-5")))
                .thenReturn(Optional.of(futureReservation("apt-user-5", "10:00", "alice@example.com")));
        when(appointmentRepository.findAll()).thenReturn(List.of(oldSlot, newSlot));
        when(appointmentBookingRepository.update(any(Appointment.class))).thenReturn(true);

        // Act
        BookingStatus result = appointmentBookingService.modifyOwnAppointment("apt-user-5", "11:00");

        // Assert
        assertEquals(BookingStatus.SUCCESS, result);
        assertTrue(oldSlot.isAvailable());
        assertFalse(newSlot.isAvailable());
        verify(eventManager).notifyObservers("Reservation modified: apt-user-5 -> 11:00");
    }

    @Test
    void modifyOwnAppointment_UserOtherReservation_ReturnsUnauthorized() {
        // Arrange
        authenticateAsUser("alice@example.com");
        when(appointmentBookingRepository.findById(eq("apt-user-6")))
                .thenReturn(Optional.of(futureReservation("apt-user-6", "10:00", "bob@example.com")));

        // Act
        BookingStatus result = appointmentBookingService.modifyOwnAppointment("apt-user-6", "11:00");

        // Assert
        assertEquals(BookingStatus.UNAUTHORIZED, result);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
    }

    @Test
    void modifyOwnAppointment_UserPastReservation_ReturnsAppointmentNotFuture() {
        // Arrange
        authenticateAsUser("alice@example.com");
        Appointment past = new Appointment(
                "apt-user-7",
                "alice@example.com",
                LocalDate.now().minusDays(1).atTime(LocalTime.parse("10:00")),
                60,
                2,
                AppointmentStatus.CONFIRMED
        );
        when(appointmentBookingRepository.findById(eq("apt-user-7"))).thenReturn(Optional.of(past));

        // Act
        BookingStatus result = appointmentBookingService.modifyOwnAppointment("apt-user-7", "11:00");

        // Assert
        assertEquals(BookingStatus.APPOINTMENT_NOT_FUTURE, result);
        verify(appointmentBookingRepository, never()).update(any(Appointment.class));
    }

    @Test
    void modifyOwnAppointment_WhenUnauthenticated_ReturnsUnauthorized() {
        // Arrange
        when(sessionManager.isLoggedIn()).thenReturn(false);

        // Act
        BookingStatus result = appointmentBookingService.modifyOwnAppointment("apt-user-8", "11:00");

        // Assert
        assertEquals(BookingStatus.UNAUTHORIZED, result);
    }

    @Test
    void bookAppointment_AssessmentTypeWithShortDuration_ReturnsInvalidAppointmentRules() {
        BookingStatus result = appointmentBookingService.bookAppointment(
                "alice@example.com",
                "10:00",
                30, // أقل من المطلوب
                1,
                AppointmentType.ASSESSMENT
        );

        assertEquals(BookingStatus.INVALID_APPOINTMENT_RULES, result);
    }
    @Test
    void bookAppointment_AssessmentTypeValid_ReturnsSuccess() {
        AppointmentSlot slot = new AppointmentSlot("10:00");
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        BookingStatus result = appointmentBookingService.bookAppointment(
                "alice@example.com",
                "10:00",
                90,
                1,
                AppointmentType.ASSESSMENT
        );

        assertEquals(BookingStatus.SUCCESS, result);
    }

    @Test
    void bookAppointment_IndividualTypeWithMultipleParticipants_ReturnsInvalidAppointmentRules() {
        BookingStatus result = appointmentBookingService.bookAppointment(
                "alice@example.com",
                "10:00",
                60,
                2, // غلط
                AppointmentType.INDIVIDUAL
        );

        assertEquals(BookingStatus.INVALID_APPOINTMENT_RULES, result);
    }

    @Test
    void bookAppointment_IndividualTypeValid_ReturnsSuccess() {
        AppointmentSlot slot = new AppointmentSlot("10:00");
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        BookingStatus result = appointmentBookingService.bookAppointment(
                "alice@example.com",
                "10:00",
                60,
                1,
                AppointmentType.INDIVIDUAL
        );

        assertEquals(BookingStatus.SUCCESS, result);
    }
    @Test
    void bookAppointment_VirtualTypeValid_ReturnsSuccess() {
        AppointmentSlot slot = new AppointmentSlot("10:00");
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        BookingStatus result = appointmentBookingService.bookAppointment(
                "alice@example.com",
                "10:00",
                60,
                2,
                AppointmentType.VIRTUAL
        );

        assertEquals(BookingStatus.SUCCESS, result);
    }
    @Test
    void bookAppointment_VirtualTypeTooManyParticipants_ReturnsInvalidAppointmentRules() {
        BookingStatus result = appointmentBookingService.bookAppointment(
                "alice@example.com",
                "10:00",
                60,
                5,
                AppointmentType.VIRTUAL
        );

        assertEquals(BookingStatus.INVALID_APPOINTMENT_RULES, result);
    }
    @Test
    void bookAppointment_InPersonTypeValid_ReturnsSuccess() {
        AppointmentSlot slot = new AppointmentSlot("10:00");
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        BookingStatus result = appointmentBookingService.bookAppointment(
                "alice@example.com",
                "10:00",
                60,
                1,
                AppointmentType.IN_PERSON
        );

        assertEquals(BookingStatus.SUCCESS, result);
    }
    @Test
    void bookAppointment_InPersonTypeWithZeroParticipants_ReturnsInvalidAppointmentRules() {
        BookingStatus result = appointmentBookingService.bookAppointment(
                "alice@example.com",
                "10:00",
                60,
                0,
                AppointmentType.IN_PERSON
        );

        assertEquals(BookingStatus.INVALID_APPOINTMENT_RULES, result);
    }

    @Test
    void bookAppointment_FollowUpTypeAtLimits_ReturnsSuccess() {
        // Arrange
        AppointmentSlot slot = new AppointmentSlot("10:00");
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        // Act
        BookingStatus result = appointmentBookingService.bookAppointment(
                "alice@example.com",
                "10:00",
                60,
                2,
                AppointmentType.FOLLOW_UP
        );

        // Assert
        assertEquals(BookingStatus.SUCCESS, result);
    }

    @Test
    void bookAppointment_FollowUpTypeWithDurationAboveLimit_ReturnsInvalidAppointmentRules() {
        // Arrange / Act
        BookingStatus result = appointmentBookingService.bookAppointment(
                "alice@example.com",
                "10:00",
                61,
                2,
                AppointmentType.FOLLOW_UP
        );

        // Assert
        assertEquals(BookingStatus.INVALID_APPOINTMENT_RULES, result);
        verifyNoInteractions(appointmentRepository);
        verifyNoInteractions(appointmentBookingRepository);
    }

    @Test
    void bookAppointment_UrgentTypeAtDurationLimit_ReturnsSuccess() {
        // Arrange
        AppointmentSlot slot = new AppointmentSlot("10:00");
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        // Act
        BookingStatus result = appointmentBookingService.bookAppointment(
                "alice@example.com",
                "10:00",
                30,
                1,
                AppointmentType.URGENT
        );

        // Assert
        assertEquals(BookingStatus.SUCCESS, result);
    }

    @Test
    void bookAppointment_GroupTypeWithTooFewParticipants_ReturnsInvalidAppointmentRules() {
        // Arrange / Act
        BookingStatus result = appointmentBookingService.bookAppointment(
                "alice@example.com",
                "10:00",
                60,
                2,
                AppointmentType.GROUP
        );

        // Assert
        assertEquals(BookingStatus.INVALID_APPOINTMENT_RULES, result);
        verifyNoInteractions(appointmentRepository);
        verifyNoInteractions(appointmentBookingRepository);
    }

    @Test
    void bookAppointment_NormalTypeExplicit_ReturnsSuccess() {
        // Arrange
        AppointmentSlot slot = new AppointmentSlot("10:00");
        when(appointmentRepository.findAll()).thenReturn(List.of(slot));

        // Act
        BookingStatus result = appointmentBookingService.bookAppointment(
                "alice@example.com",
                "10:00",
                60,
                1,
                AppointmentType.NORMAL
        );

        // Assert
        assertEquals(BookingStatus.SUCCESS, result);
    }

    private void authenticateAsAdmin() {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.isAdmin()).thenReturn(true);
    }

    private void authenticateAsUser(String email) {
        when(sessionManager.isLoggedIn()).thenReturn(true);
        when(sessionManager.getCurrentEmail()).thenReturn(email);
    }

    private Appointment futureReservation(String id, String slotTime, String customerEmail) {
        return new Appointment(
                id,
                customerEmail,
                LocalDate.now().plusDays(1).atTime(LocalTime.parse(slotTime)),
                60,
                2,
                AppointmentStatus.CONFIRMED
        );
    }

    private Appointment defaultPastReservation() {
        return new Appointment(
                "apt-32",
                "alice@example.com",
                LocalDate.now().minusDays(1).atTime(LocalTime.parse("10:00")),
                60,
                2,
                AppointmentStatus.CONFIRMED
        );
    }

}
