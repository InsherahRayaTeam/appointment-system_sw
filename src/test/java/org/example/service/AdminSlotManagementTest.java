package org.example.service;

import org.example.domain.UserRole;
import org.example.repository.AdminRepository;
import org.example.repository.AppointmentBookingRepository;
import org.example.repository.AppointmentRepository;
import org.example.repository.InMemoryAdminRepository;
import org.example.repository.InMemoryAppointmentBookingRepository;
import org.example.repository.InMemoryAppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for admin-only slot management operations.
 *
 * @author appointment-system
 * @version 1.0
 */
public class AdminSlotManagementTest {

    private AppointmentBookingService appointmentBookingService;
    private AppointmentService appointmentService;
    private SessionManager sessionManager;
    private AuthEventLogger authEventLogger;
    private EventManager eventManager;
    private AdminRepository adminRepository;
    private AppointmentRepository appointmentRepository;
    private AppointmentBookingRepository appointmentBookingRepository;

    @BeforeEach
    public void setup() {
        authEventLogger = new AuthEventLogger();
        eventManager = new EventManager();
        adminRepository = new InMemoryAdminRepository();
        appointmentRepository = new InMemoryAppointmentRepository();
        appointmentBookingRepository = new InMemoryAppointmentBookingRepository();
        sessionManager = new SessionManager(authEventLogger, eventManager);

        appointmentService = new AppointmentService(appointmentRepository, eventManager);
        appointmentBookingService = new AppointmentBookingService(
                appointmentRepository,
                appointmentBookingRepository,
                sessionManager,
                adminRepository,
                eventManager
        );
    }

    @Test
    public void testAdminCanAddSlot() {
        // Arrange
        sessionManager.login("admin-user", UserRole.ADMIN);

        // Act
        BookingStatus status = appointmentBookingService.addManagedSlot("15:00");

        // Assert
        assertEquals(BookingStatus.SUCCESS, status);
        assertTrue(appointmentRepository.findByTime("15:00").isPresent());
    }

    @Test
    public void testAdminCannotAddDuplicateSlot() {
        // Arrange
        sessionManager.login("admin-user", UserRole.ADMIN);
        appointmentBookingService.addManagedSlot("15:00");

        // Act - attempt to add same slot again
        BookingStatus status = appointmentBookingService.addManagedSlot("15:00");

        // Assert
        assertEquals(BookingStatus.SLOT_ALREADY_BOOKED, status);
    }

    @Test
    public void testAdminCanCancelSlot() {
        // Arrange
        sessionManager.login("admin-user", UserRole.ADMIN);
        appointmentBookingService.addManagedSlot("15:00");

        // Act
        BookingStatus status = appointmentBookingService.cancelManagedSlot("15:00");

        // Assert
        assertEquals(BookingStatus.SUCCESS, status);
        assertFalse(appointmentRepository.findByTime("15:00").isPresent());
    }

    @Test
    public void testCancelledSlotNotBookable() {
        // Arrange
        sessionManager.login("admin-user", UserRole.ADMIN);
        appointmentBookingService.addManagedSlot("15:00");
        appointmentBookingService.cancelManagedSlot("15:00");
        sessionManager.logout();
        sessionManager.login("user1", UserRole.USER);

        // Act
        boolean booked = appointmentService.bookSlot("15:00");

        // Assert
        assertFalse(booked, "Cancelled slot should not be bookable");
    }

    @Test
    public void testNonAdminCannotAddSlot() {
        // Arrange
        sessionManager.login("regular-user", UserRole.USER);

        // Act
        BookingStatus status = appointmentBookingService.addManagedSlot("15:00");

        // Assert
        assertEquals(BookingStatus.UNAUTHORIZED, status);
    }

    @Test
    public void testNonAdminCannotCancelSlot() {
        // Arrange
        sessionManager.login("regular-user", UserRole.USER);

        // Act
        BookingStatus status = appointmentBookingService.cancelManagedSlot("10:00");

        // Assert
        assertEquals(BookingStatus.UNAUTHORIZED, status);
    }

    @Test
    public void testUnauthenticatedUserCannotAddSlot() {
        // Arrange - no login

        // Act
        BookingStatus status = appointmentBookingService.addManagedSlot("15:00");

        // Assert
        assertEquals(BookingStatus.UNAUTHORIZED, status);
    }

    @Test
    public void testAddSlotWithBlankTimeReturnsError() {
        // Arrange
        sessionManager.login("admin-user", UserRole.ADMIN);

        // Act
        BookingStatus status1 = appointmentBookingService.addManagedSlot("");
        BookingStatus status2 = appointmentBookingService.addManagedSlot(null);

        // Assert
        assertEquals(BookingStatus.BLANK_SLOT_TIME, status1);
        assertEquals(BookingStatus.BLANK_SLOT_TIME, status2);
    }

    @Test
    public void testCancelNonexistentSlotReturnsNotFound() {
        // Arrange
        sessionManager.login("admin-user", UserRole.ADMIN);

        // Act
        BookingStatus status = appointmentBookingService.cancelManagedSlot("99:99");

        // Assert
        assertEquals(BookingStatus.SLOT_NOT_FOUND, status);
    }

    @Test
    public void testAddedSlotAppearsInAvailableSlots() {
        // Arrange
        sessionManager.login("admin-user", UserRole.ADMIN);
        int initialCount = appointmentService.getAvailableSlots().size();

        // Act
        appointmentBookingService.addManagedSlot("15:00");

        // Assert
        int finalCount = appointmentService.getAvailableSlots().size();
        assertEquals(initialCount + 1, finalCount);
        assertTrue(
                appointmentService.getAvailableSlots().stream()
                        .anyMatch(slot -> slot.getTime().equals("15:00")),
                "Added slot should appear in available slots"
        );
    }

    @Test
    public void testCancelledSlotDoesNotAppearInAvailableSlots() {
        // Arrange
        sessionManager.login("admin-user", UserRole.ADMIN);
        appointmentBookingService.addManagedSlot("15:00");
        assertTrue(
                appointmentService.getAvailableSlots().stream()
                        .anyMatch(slot -> slot.getTime().equals("15:00")),
                "Slot should be available before cancellation"
        );

        // Act
        appointmentBookingService.cancelManagedSlot("15:00");

        // Assert
        assertFalse(
                appointmentService.getAvailableSlots().stream()
                        .anyMatch(slot -> slot.getTime().equals("15:00")),
                "Cancelled slot should not appear in available slots"
        );
    }

    @Test
    public void testAdminCannotBookAppointments() {
        // Arrange
        sessionManager.login("admin-user", UserRole.ADMIN);

        // Act
        BookingStatus status = appointmentBookingService.bookAppointment(
                "admin-name",
                "10:00",
                60,
                1
        );

        // Assert
        assertEquals(BookingStatus.UNAUTHORIZED, status);
    }

    @Test
    public void testUserCanBookAvailableSlot() {
        // Arrange
        sessionManager.login("user1", UserRole.USER);

        // Act
        BookingStatus status = appointmentBookingService.bookAppointment(
                "user1",
                "10:00",
                60,
                1
        );

        // Assert
        assertEquals(BookingStatus.SUCCESS, status);
    }

    @Test
    public void testUserCannotBookCancelledSlot() {
        // Arrange
        sessionManager.login("admin-user", UserRole.ADMIN);
        appointmentBookingService.cancelManagedSlot("10:00");
        sessionManager.logout();
        sessionManager.login("user1", UserRole.USER);

        // Act
        BookingStatus status = appointmentBookingService.bookAppointment(
                "user1",
                "10:00",
                60,
                1
        );

        // Assert
        assertEquals(BookingStatus.SLOT_NOT_FOUND, status);
    }
}

