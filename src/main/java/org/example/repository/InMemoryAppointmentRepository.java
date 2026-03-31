package org.example.repository;

import org.example.domain.AppointmentSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
     * Returns all slots excluding cancelled slots.
     *
     * @return list of all non-cancelled slots
     */
    @Override
    public List<AppointmentSlot> findAll() {
        List<AppointmentSlot> result = new ArrayList<>();
        for (AppointmentSlot slot : slots) {
            if (!slot.isCancelled()) {
                result.add(slot);
            }
        }
        return result;
    }

    /**
     * Returns only available (not booked and not cancelled) slots.
     *
     * @return list of available slots
     */
    @Override
    public List<AppointmentSlot> findAvailable() {
        List<AppointmentSlot> available = new ArrayList<>();
        for (AppointmentSlot slot : slots) {
            if (slot.isAvailable() && !slot.isCancelled()) {
                available.add(slot);
            }
        }
        return available;
    }

    /**
     * Finds a slot by time, returning it only if not cancelled.
     *
     * @param time slot time identifier
     * @return Optional containing the slot if found and not cancelled
     */
    @Override
    public Optional<AppointmentSlot> findByTime(String time) {
        if (time == null || time.trim().isEmpty()) {
            return Optional.empty();
        }
        String normalizedTime = time.trim();
        for (AppointmentSlot slot : slots) {
            if (slot.getTime().equals(normalizedTime) && !slot.isCancelled()) {
                return Optional.of(slot);
            }
        }
        return Optional.empty();
    }

    /**
     * Saves a new appointment slot if the time does not already exist.
     *
     * @param slot the slot to save
     * @return true if the slot was saved, false if it already exists
     */
    @Override
    public boolean save(AppointmentSlot slot) {
        if (slot == null || slot.getTime() == null || slot.getTime().trim().isEmpty()) {
            return false;
        }
        String normalizedTime = slot.getTime().trim();
        for (AppointmentSlot existing : slots) {
            if (existing.getTime().equals(normalizedTime)) {
                return false;
            }
        }
        slots.add(slot);
        return true;
    }

    /**
     * Removes (cancels) a slot by time.
     *
     * @param time the slot time identifier to remove
     * @return true if the slot was removed, false if not found
     */
    @Override
    public boolean removeSlot(String time) {
        if (time == null || time.trim().isEmpty()) {
            return false;
        }
        String normalizedTime = time.trim();
        for (AppointmentSlot slot : slots) {
            if (slot.getTime().equals(normalizedTime) && !slot.isCancelled()) {
                slot.cancel();
                return true;
            }
        }
        return false;
    }
}
