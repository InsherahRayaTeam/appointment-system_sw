package org.example.repository;

import org.example.domain.AppointmentSlot;

import java.util.List;

/**
 * Repository interface for accessing appointment slot data.
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
}



