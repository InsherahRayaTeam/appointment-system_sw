package org.example.repository;

import org.example.domain.Appointment;

import java.util.List;
import java.util.Optional;

/**
 * Defines the operations for appointment booking repository.
 */
public interface AppointmentBookingRepository {

    /**
     * Saves one appointment record.
     *
     * @param appointment appointment to store
     */
    void save(Appointment appointment);

    /**
     * Finds all using the given input.
     *
     * @return collection with the requested results
     */
    List<Appointment> findAll();

    /**
     * Finds by id using the given input.
     *
     * @param appointmentId unique id used to find the record
     *
     * @return optional value if data is found
     */
    Optional<Appointment> findById(String appointmentId);

    /**
     * Updates an existing appointment record.
     *
     * @param appointment appointment with updated values
     *
     * @return true when the action is valid or successful, otherwise false
     */
    boolean update(Appointment appointment);
}

