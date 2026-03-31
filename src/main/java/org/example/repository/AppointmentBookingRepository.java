package org.example.repository;

import org.example.domain.Appointment;

import java.util.List;
import java.util.Optional;

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

    /**
     * Finds a booked appointment by identifier.
     *
     * @param appointmentId appointment identifier
     * @return optional appointment when found
     */
    Optional<Appointment> findById(String appointmentId);

    /**
     * Replaces an existing appointment by id.
     *
     * @param appointment updated appointment state
     * @return true when an existing appointment was updated
     */
    boolean update(Appointment appointment);

    /**
     * Finds all appointments for a specific customer (used for "My Appointments" flow).
     *
     * @param customerName the customer name
     * @return list of appointments for this customer
     */
    List<Appointment> findByCustomerName(String customerName);

    /**
     * Removes an appointment from the repository.
     *
     * @param appointmentId appointment identifier
     * @return true when an existing appointment was removed
     */
    boolean remove(String appointmentId);
}

