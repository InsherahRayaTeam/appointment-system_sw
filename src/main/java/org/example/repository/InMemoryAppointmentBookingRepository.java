package org.example.repository;

import org.example.domain.Appointment;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory repository implementation for booked appointments.
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

    private Appointment copyOf(Appointment appointment) {
        return new Appointment(
                appointment.getCustomerName(),
                appointment.getSlotTime(),
                appointment.getDurationMinutes(),
                appointment.getParticipantCount(),
                appointment.getStatus()
        );
    }
}

