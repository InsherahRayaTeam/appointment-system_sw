package org.example.repository;

import org.example.domain.Appointment;
import org.example.domain.AppointmentType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents in memory appointment booking repository in the system.
 */
public class InMemoryAppointmentBookingRepository implements AppointmentBookingRepository {

    private final List<Appointment> appointments = new ArrayList<>();

    /**
     * Runs save for this class.
     *
     * @param appointment value for appointment
     */
    @Override
    public void save(Appointment appointment) {
        appointments.add(copyOf(appointment));
    }

    /**
     * Finds all using the given input.
     *
     * @return collection with the requested results
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
     * Finds by id using the given input.
     *
     * @param appointmentId unique id used to find the record
     *
     * @return optional value if data is found
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
     * Runs update for this class.
     *
     * @param appointment value for appointment
     *
     * @return true when the action is valid or successful, otherwise false
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

    private Appointment copyOf(Appointment appointment) {
        return new Appointment(
                appointment.getId(),
                appointment.getCustomerName(),
                appointment.getCustomerEmail(),
                appointment.getStartTime(),
                appointment.getDurationMinutes(),
                appointment.getParticipantCount(),
                appointment.getStatus(),
                appointment.getType() == null ? AppointmentType.NORMAL : appointment.getType()
        );
    }
}

