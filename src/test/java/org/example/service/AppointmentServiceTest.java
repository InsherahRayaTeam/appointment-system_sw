package org.example.service;

import org.example.domain.AppointmentSlot;
import org.example.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentServiceTest {

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService();
    }

    @Test
    void getAvailableSlots_initiallyContainsAllSlots() {
        List<AppointmentSlot> slots = appointmentService.getAvailableSlots();
        assertEquals(3, slots.size());
    }

    @Test
    void bookSlot_validSlot_succeeds() {
        boolean result = appointmentService.bookSlot("10:00");
        assertTrue(result);
    }

    @Test
    void bookSlot_sameSlotTwice_failsSecondTime() {
        appointmentService.bookSlot("10:00");
        boolean result = appointmentService.bookSlot("10:00");
        assertFalse(result);
    }

    @Test
    void bookSlot_nonExistingSlot_fails() {
        boolean result = appointmentService.bookSlot("15:00");
        assertFalse(result);
    }

    @Test
    void bookSlot_nullTime_fails() {
        boolean result = appointmentService.bookSlot(null);
        assertFalse(result);
    }

    @Test
    void bookSlot_blankTime_fails() {
        boolean result = appointmentService.bookSlot("   ");
        assertFalse(result);
    }

    @Test
    void getAvailableSlots_afterBooking_decreases() {
        appointmentService.bookSlot("10:00");
        List<AppointmentSlot> slots = appointmentService.getAvailableSlots();
        assertEquals(2, slots.size());
    }

    @Test
    void getAvailableSlots_UsesRepositoryAndFiltersBookedSlots() {
        AppointmentSlot available = new AppointmentSlot("09:00");
        AppointmentSlot booked = new AppointmentSlot("09:30");
        booked.book();

        AppointmentRepository repository = new AppointmentRepository() {
            @Override
            public List<AppointmentSlot> findAll() {
                List<AppointmentSlot> slots = new ArrayList<>();
                slots.add(available);
                slots.add(booked);
                return slots;
            }

            @Override
            public List<AppointmentSlot> findAvailable() {
                List<AppointmentSlot> availableSlots = new ArrayList<>();
                availableSlots.add(available);
                return availableSlots;
            }
        };

        AppointmentService service = new AppointmentService(repository);

        List<AppointmentSlot> result = service.getAvailableSlots();

        assertEquals(1, result.size());
        assertEquals("09:00", result.get(0).getTime());
    }

    @Test
    void getAvailableSlots_returnsOnlyAvailableSlots() {
        AppointmentSlot slot1 = new AppointmentSlot("09:00");
        AppointmentSlot slot2 = new AppointmentSlot("09:30");
        slot2.book();
        AppointmentSlot slot3 = new AppointmentSlot("10:00");

        AppointmentService service = serviceWithSlots(slot1, slot2, slot3);

        List<AppointmentSlot> result = service.getAvailableSlots();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(AppointmentSlot::isAvailable));
    }

    @Test
    void getAvailableSlots_doesNotIncludeBookedSlots() {
        AppointmentSlot booked = new AppointmentSlot("10:00");
        booked.book();
        AppointmentSlot available = new AppointmentSlot("11:00");

        AppointmentService service = serviceWithSlots(booked, available);

        List<AppointmentSlot> result = service.getAvailableSlots();

        assertFalse(result.stream().anyMatch(slot -> "10:00".equals(slot.getTime())));
        assertTrue(result.stream().anyMatch(slot -> "11:00".equals(slot.getTime())));
    }

    @Test
    void getAvailableSlots_withEmptyList_returnsEmptyList() {
        AppointmentService service = serviceWithSlots();

        List<AppointmentSlot> result = service.getAvailableSlots();

        assertTrue(result.isEmpty());
    }

    private AppointmentService serviceWithSlots(AppointmentSlot... slots) {
        AppointmentRepository repository = new AppointmentRepository() {
            @Override
            public List<AppointmentSlot> findAll() {
                List<AppointmentSlot> all = new ArrayList<>();
                for (AppointmentSlot slot : slots) {
                    all.add(slot);
                }
                return all;
            }

            @Override
            public List<AppointmentSlot> findAvailable() {
                List<AppointmentSlot> available = new ArrayList<>();
                for (AppointmentSlot slot : slots) {
                    if (slot.isAvailable()) {
                        available.add(slot);
                    }
                }
                return available;
            }
        };

        return new AppointmentService(repository);
    }
}