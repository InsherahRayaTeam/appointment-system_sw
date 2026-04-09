package org.example.repository;

import org.example.domain.AppointmentSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents in memory appointment repository in the system.
 */
public class InMemoryAppointmentRepository implements AppointmentRepository {

    private final List<AppointmentSlot> slots = new ArrayList<>();

    /**
     * Creates a new in memory appointment repository object with the given values.
     */
    public InMemoryAppointmentRepository() {
        LocalDate firstAvailableDate = LocalDate.now().plusDays(1);
        slots.add(new AppointmentSlot(firstAvailableDate, LocalTime.of(10, 0)));
        slots.add(new AppointmentSlot(firstAvailableDate, LocalTime.of(11, 0)));
        slots.add(new AppointmentSlot(firstAvailableDate, LocalTime.of(12, 0)));
    }

    /**
     * Finds all using the given input.
     *
     * @return collection with the requested results
     */
    @Override
    public List<AppointmentSlot> findAll() {
        return new ArrayList<>(slots);
    }

    /**
     * Finds available using the given input.
     *
     * @return collection with the requested results
     */
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
}
