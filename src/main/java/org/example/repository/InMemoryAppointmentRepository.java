package org.example.repository;

import org.example.domain.AppointmentSlot;

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
        slots.add(new AppointmentSlot("10:00"));
        slots.add(new AppointmentSlot("11:00"));
        slots.add(new AppointmentSlot("12:00"));
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
