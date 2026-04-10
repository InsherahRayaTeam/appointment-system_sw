package org.example.repository;

import org.example.domain.AppointmentSlot;

import java.util.List;

/**
 * Defines the operations for appointment repository.
 */
public interface AppointmentRepository {

    /**
     * Finds all using the given input.
     *
     * @return collection with the requested results
     */
    List<AppointmentSlot> findAll();

    /**
     * Finds available using the given input.
     *
     * @return collection with the requested results
     */
    List<AppointmentSlot> findAvailable();

    /**
     * Adds a slot when it is valid and not duplicated.
     *
     * @param slot slot value used by this method
     * @return true when the action is valid or successful, otherwise false
     */
    boolean addSlot(AppointmentSlot slot);
}



