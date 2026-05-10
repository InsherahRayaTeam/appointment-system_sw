package org.example.repository;

import org.example.domain.Appointment;
import org.example.domain.AppointmentDetails;
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
     * Saves one appointment in memory.
     *
     * @param appointment appointment to store
     */
    @Override
    public void save(Appointment appointment) {
        if (appointment == null) {
            return;
        }

        appointments.add(copyOf(appointment));
    }

    /**
     * Finds all appointments.
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
     * Finds appointment by id.
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
     * Updates one appointment in memory.
     *
     * @param appointment appointment with updated values
     *
     * @return true when the action is valid or successful, otherwise false
     */
    @Override
    public boolean update(Appointment appointment) {
        if (appointment == null
                || appointment.getId() == null
                || appointment.getId().trim().isEmpty()) {
            return false;
        }

        String normalizedId = appointment.getId().trim();

        for (int i = 0; i < appointments.size(); i++) {
            Appointment current = appointments.get(i);

            if (normalizedId.equals(current.getId())) {
                appointments.set(i, copyOf(appointment));
                return true;
            }
        }

        return false;
    }

    /**
     * Creates a defensive copy of an appointment.
     *
     * @param appointment appointment to copy
     *
     * @return copied appointment
     */
    private Appointment copyOf(Appointment appointment) {
        AppointmentDetails details = new AppointmentDetails(
                appointment.getCustomerName(),
                appointment.getUser() == null ? null : appointment.getUser().getEmail(),
                appointment.getCustomerPhoneNumber(),
                appointment.getStartTime(),
                appointment.getDurationMinutes(),
                appointment.getParticipantCount()
        );

        Appointment copy = new Appointment(
                appointment.getId(),
                details,
                appointment.getStatus(),
                appointment.getType() == null ? AppointmentType.NORMAL : appointment.getType()
        );

        copy.setRating(appointment.getRating());
        copy.setFeedbackComment(appointment.getFeedbackComment());
        copy.setFeedbackSubmitted(appointment.isFeedbackSubmitted());

        return copy;
    }
}