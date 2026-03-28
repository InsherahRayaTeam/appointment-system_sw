package org.example.repository;

import org.example.domain.Appointment;

import java.util.List;

/**
 * Repository interface for storing and retrieving booked appointments.
 *
 * @author appointment-system
 * @version 1.0
 */
public interface AppointmentBookingRepository {

    /**
     * Saves a booked appointment.
     *
     * @param appointment the appointment to save
     */
    void save(Appointment appointment);

    /**
     * Retrieves all booked appointments.
     *
     * @return a list of all booked appointments
     */
    List<Appointment> findAll();
}

