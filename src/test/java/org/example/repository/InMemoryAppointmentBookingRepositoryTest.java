package org.example.repository;

import org.example.domain.Appointment;
import org.example.domain.AppointmentDetails;
import org.example.domain.AppointmentStatus;
import org.example.domain.AppointmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryAppointmentBookingRepositoryTest {

    private InMemoryAppointmentBookingRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryAppointmentBookingRepository();
    }

    @Test
    void save_NullAppointment_ShouldNotStoreAnything() {
        repository.save(null);

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void save_AppointmentPersistsDefensiveCopy() {
        Appointment appointment = appointment(
                "apt-1",
                "Alice",
                "2026-01-01T10:00:00",
                60,
                2,
                AppointmentStatus.CONFIRMED
        );
        appointment.setRating(4);
        appointment.setFeedbackComment("Nice visit");
        appointment.setFeedbackSubmitted(true);

        repository.save(appointment);

        Optional<Appointment> loaded = repository.findById("apt-1");

        assertTrue(loaded.isPresent());
        assertNotSame(appointment, loaded.get());
        assertEquals("apt-1", loaded.get().getId());
        assertEquals("Alice", loaded.get().getCustomerName());
        assertEquals(LocalDateTime.parse("2026-01-01T10:00:00"), loaded.get().getStartTime());
        assertEquals(60, loaded.get().getDurationMinutes());
        assertEquals(2, loaded.get().getParticipantCount());
        assertEquals(AppointmentStatus.CONFIRMED, loaded.get().getStatus());
        assertEquals(4, loaded.get().getRating());
        assertEquals("Nice visit", loaded.get().getFeedbackComment());
        assertTrue(loaded.get().isFeedbackSubmitted());
    }

    @Test
    void findAll_WhenRepositoryIsEmpty_ReturnsEmptyList() {
        List<Appointment> appointments = repository.findAll();

        assertTrue(appointments.isEmpty());
    }

    @Test
    void findAll_ReturnsDefensiveCopiesAndIndependentSnapshot() {
        Appointment first = appointment(
                "apt-1",
                "Alice",
                "2026-01-01T10:00:00",
                60,
                2,
                AppointmentStatus.CONFIRMED
        );
        Appointment second = appointment(
                "apt-2",
                "Bob",
                "2026-01-01T11:00:00",
                30,
                1,
                AppointmentStatus.MODIFIED
        );

        repository.save(first);
        repository.save(second);

        List<Appointment> snapshot = repository.findAll();

        assertEquals(2, snapshot.size());
        assertNotSame(first, snapshot.get(0));
        assertNotSame(second, snapshot.get(1));

        snapshot.clear();

        List<Appointment> afterClear = repository.findAll();

        assertEquals(2, afterClear.size());
        assertEquals("apt-1", afterClear.get(0).getId());
        assertEquals("apt-2", afterClear.get(1).getId());
    }

    @Test
    void findById_WithNullOrBlankInput_ReturnsEmptyOptional() {
        assertTrue(repository.findById(null).isEmpty());
        assertTrue(repository.findById("").isEmpty());
        assertTrue(repository.findById("   ").isEmpty());
    }

    @Test
    void findById_WithTrimmedIdentifier_ReturnsMatchingAppointment() {
        repository.save(appointment(
                "apt-1",
                "Alice",
                "2026-01-01T10:00:00",
                60,
                2,
                AppointmentStatus.CONFIRMED
        ));

        Optional<Appointment> loaded = repository.findById("  apt-1  ");

        assertTrue(loaded.isPresent());
        assertEquals("apt-1", loaded.get().getId());
        assertEquals("Alice", loaded.get().getCustomerName());
    }

    @Test
    void findById_WithUnknownIdentifier_ReturnsEmptyOptional() {
        repository.save(appointment(
                "apt-1",
                "Alice",
                "2026-01-01T10:00:00",
                60,
                2,
                AppointmentStatus.CONFIRMED
        ));

        assertTrue(repository.findById("missing").isEmpty());
    }

    @Test
    void update_NullOrBlankIdentifier_ReturnsFalse() {
        assertFalse(repository.update(null));
        assertFalse(repository.update(appointment(
                null,
                "Alice",
                "2026-01-01T10:00:00",
                60,
                2,
                AppointmentStatus.CONFIRMED
        )));
        assertFalse(repository.update(appointment(
                "   ",
                "Alice",
                "2026-01-01T10:00:00",
                60,
                2,
                AppointmentStatus.CONFIRMED
        )));
    }

    @Test
    void update_WithMissingIdentifier_ReturnsFalse() {
        repository.save(appointment(
                "apt-1",
                "Alice",
                "2026-01-01T10:00:00",
                60,
                2,
                AppointmentStatus.CONFIRMED
        ));

        boolean updated = repository.update(appointment(
                "apt-2",
                "Bob",
                "2026-01-01T11:00:00",
                30,
                1,
                AppointmentStatus.CANCELLED
        ));

        assertFalse(updated);
        assertEquals("Alice", repository.findById("apt-1").orElseThrow().getCustomerName());
    }

    @Test
    void update_WithTrimmedIdentifier_ShouldMatchStoredIdentifier() {
        Appointment original = appointment(
                "apt-1",
                "Alice",
                "2026-04-20T10:00:00",
                60,
                1,
                AppointmentStatus.CONFIRMED
        );
        repository.save(original);

        AppointmentDetails updatedDetails = new AppointmentDetails(
                "Alice Updated",
                "alice.updated@example.com",
                "0598888888",
                LocalDateTime.of(2026, 4, 20, 12, 0),
                90,
                2
        );

        Appointment updated = new Appointment(
                "  apt-1  ",
                updatedDetails,
                AppointmentStatus.MODIFIED,
                AppointmentType.NORMAL
        );

        boolean result = repository.update(updated);

        assertTrue(result);

        Appointment stored = repository.findById("apt-1").orElseThrow();

        assertEquals("Alice Updated", stored.getCustomerName());
        assertEquals(AppointmentStatus.MODIFIED, stored.getStatus());
        assertEquals("12:00", stored.getSlotTime());
    }

    @Test
    void update_WithExistingIdentifier_ReplacesStoredAppointment() {
        repository.save(appointment(
                "apt-1",
                "Alice",
                "2026-01-01T10:00:00",
                60,
                2,
                AppointmentStatus.CONFIRMED
        ));

        Appointment replacement = appointment(
                "apt-1",
                "Alice Updated",
                "2026-01-01T11:00:00",
                45,
                4,
                AppointmentStatus.MODIFIED
        );
        replacement.setRating(5);
        replacement.setFeedbackComment("Updated feedback");
        replacement.setFeedbackSubmitted(true);

        boolean updated = repository.update(replacement);

        assertTrue(updated);

        Appointment stored = repository.findById("apt-1").orElseThrow();

        assertEquals("Alice Updated", stored.getCustomerName());
        assertEquals(LocalDateTime.parse("2026-01-01T11:00:00"), stored.getStartTime());
        assertEquals(45, stored.getDurationMinutes());
        assertEquals(4, stored.getParticipantCount());
        assertEquals(AppointmentStatus.MODIFIED, stored.getStatus());
        assertEquals(5, stored.getRating());
        assertEquals("Updated feedback", stored.getFeedbackComment());
        assertTrue(stored.isFeedbackSubmitted());
        assertNotSame(replacement, stored);
    }

    private Appointment appointment(
            String id,
            String customerName,
            String startTime,
            int duration,
            int participants,
            AppointmentStatus status
    ) {
        return new Appointment(
                id,
                customerName,
                LocalDateTime.parse(startTime),
                duration,
                participants,
                status
        );
    }
}