package org.example.repository;

import org.example.domain.Appointment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * In-memory repository implementation for booked appointments.
 *
 * @author appointment-system
 * @version 1.0
 */
public class InMemoryAppointmentBookingRepository implements AppointmentBookingRepository {

    private final List<Appointment> appointments = new ArrayList<>();

    /**
     * Saves a defensive copy of the provided appointment.
     *
     * @param appointment the appointment to save
     */
    @Override
    public void save(Appointment appointment) {
        appointments.add(copyOf(appointment));
    }

    /**
     * Returns defensive copies of all stored appointments.
     *
     * @return a list containing copies of all appointments
     */
    @Override
    public List<Appointment> findAll() {
        List<Appointment> copies = new ArrayList<>();
        for (Appointment appointment : appointments) {
            copies.add(copyOf(appointment));
        }
        return copies;
    }

    /**
     * Finds an appointment by id and returns a defensive copy.
     *
     * @param appointmentId appointment identifier
     * @return optional appointment copy
     */
    @Override
    public Optional<Appointment> findById(String appointmentId) {
        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            return Optional.empty();
        }

        String normalizedId = appointmentId.trim();
        for (Appointment appointment : appointments) {
            if (normalizedId.equals(appointment.getId())) {
                return Optional.of(copyOf(appointment));
            }
        }

        return Optional.empty();
    }

    /**
     * Updates an existing appointment by id using a defensive copy.
     *
     * @param appointment updated appointment data
     * @return true when update succeeded
     */
    @Override
    public boolean update(Appointment appointment) {
        if (appointment == null || appointment.getId() == null || appointment.getId().trim().isEmpty()) {
            return false;
        }

        for (int i = 0; i < appointments.size(); i++) {
            Appointment current = appointments.get(i);
            if (appointment.getId().equals(current.getId())) {
                appointments.set(i, copyOf(appointment));
                return true;
            }
        }

        return false;
    }

    /**
     * Finds all appointments for a specific customer (used for "My Appointments" flow).
     *
     * @param customerName the customer name
     * @return list of appointments for this customer
     */
    @Override
    public List<Appointment> findByCustomerName(String customerName) {
        List<Appointment> result = new ArrayList<>();
        if (customerName == null || customerName.trim().isEmpty()) {
            return result;
        }

        String normalizedName = customerName.trim();
        for (Appointment appointment : appointments) {
            if (normalizedName.equals(appointment.getCustomerName())) {
                result.add(copyOf(appointment));
            }
        }
        return result;
    }

    /**
     * Removes an appointment from the repository.
     *
     * @param appointmentId appointment identifier
     * @return true when an existing appointment was removed
     */
    @Override
    public boolean remove(String appointmentId) {
        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            return false;
        }

        String normalizedId = appointmentId.trim();
        for (int i = 0; i < appointments.size(); i++) {
            if (normalizedId.equals(appointments.get(i).getId())) {
                appointments.remove(i);
                return true;
            }
        }
        return false;
    }

    private Appointment copyOf(Appointment appointment) {
        return new Appointment(
                appointment.getId(),
                appointment.getCustomerName(),
                appointment.getStartTime(),
                appointment.getDurationMinutes(),
                appointment.getParticipantCount(),
                appointment.getStatus()
        );
    }
}

