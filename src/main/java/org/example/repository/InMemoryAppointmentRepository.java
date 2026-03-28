package org.example.repository;

import org.example.domain.AppointmentSlot;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory appointment-slot repository seeded with demo times.
 *
 * @author appointment-system
 * @version 1.0
 */
public class InMemoryAppointmentRepository implements AppointmentRepository {

    private final List<AppointmentSlot> slots = new ArrayList<>();

    /**
     * Creates repository with predefined slot data.
     */
    public InMemoryAppointmentRepository() {
        slots.add(new AppointmentSlot("10:00"));
        slots.add(new AppointmentSlot("11:00"));
        slots.add(new AppointmentSlot("12:00"));
    }

    /**
     * Returns all slots.
     *
     * @return list of all slots
     */
    @Override
    public List<AppointmentSlot> findAll() {
        return new ArrayList<>(slots);
    }

    /**
     * Returns only available slots.
     *
     * @return list of available slots
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
