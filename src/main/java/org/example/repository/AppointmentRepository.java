package org.example.repository;

import org.example.domain.AppointmentSlot;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing appointment slot data.
 *
 * @author appointment-system
 * @version 1.0
 */
public interface AppointmentRepository {

    /**
     * Retrieves all appointment slots, regardless of availability.
     *
     * @return a list of all appointment slots
     */
    List<AppointmentSlot> findAll();

    /**
     * Retrieves only available (not yet booked) appointment slots.
     *
     * @return a list of available appointment slots
     */
    List<AppointmentSlot> findAvailable();

    /**
     * Finds a slot by its time identifier.
     *
     * @param time slot time identifier
     * @return Optional containing the slot if found
     */
    Optional<AppointmentSlot> findByTime(String time);

    /**
     * Saves a new appointment slot.
     *
     * @param slot the slot to save
     * @return true if the slot was saved successfully, false if it already exists
     */
    boolean save(AppointmentSlot slot);

    /**
     * Removes a slot from the repository (used for admin slot cancellation).
     *
     * @param time the slot time identifier to remove
     * @return true if the slot was removed, false if not found
     */
    boolean removeSlot(String time);
}



