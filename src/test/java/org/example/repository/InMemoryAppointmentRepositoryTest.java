package org.example.repository;

import org.example.domain.AppointmentSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryAppointmentRepositoryTest {

    private InMemoryAppointmentRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryAppointmentRepository();
    }

    @Test
    void findAll_ReturnsPredefinedSlots() {
        List<AppointmentSlot> all = repository.findAll();

        assertEquals(3, all.size());
        assertEquals("10:00", all.get(0).getTime());
        assertEquals("11:00", all.get(1).getTime());
        assertEquals("12:00", all.get(2).getTime());
        assertEquals(LocalDate.now().plusDays(1), all.get(0).getDate());
        assertTrue(all.get(0).isFutureSlot());
    }

    @Test
    void findAvailable_InitiallyReturnsAllSlots() {
        List<AppointmentSlot> available = repository.findAvailable();

        assertEquals(3, available.size());
        assertTrue(available.stream().allMatch(AppointmentSlot::isAvailable));
    }

    @Test
    void findAvailable_AfterBookingOneSlot_ReturnsOnlyUnbookedSlots() {
        List<AppointmentSlot> all = repository.findAll();
        all.get(0).book();

        List<AppointmentSlot> available = repository.findAvailable();

        assertEquals(2, available.size());
        assertFalse(available.stream().anyMatch(slot -> "10:00".equals(slot.getTime())));
    }
}

