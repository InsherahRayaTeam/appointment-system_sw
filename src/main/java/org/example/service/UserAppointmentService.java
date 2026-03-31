package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentStatus;
import org.example.repository.AppointmentRepository;
import org.example.repository.AppointmentBookingRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service for user-facing appointment operations.
 * Handles USER-only operations like viewing their own appointments,
 * modifying their own appointments (within future-only constraints),
 * and cancelling their own appointments (within future-only constraints).
 *
 * @author appointment-system
 * @version 1.0
 */
public class UserAppointmentService {

    private final AppointmentBookingRepository appointmentBookingRepository;
    private final AppointmentRepository appointmentRepository;
    private final SessionManager sessionManager;
    private final EventManager eventManager;
    private final TimeProvider timeProvider;

    /**
     * Creates a user appointment service.
     *
     * @param appointmentBookingRepository repository for appointment persistence
     * @param appointmentRepository repository for slot management
     * @param sessionManager session manager for user identification
     * @param eventManager event manager for notifications
     */
    public UserAppointmentService(
            AppointmentBookingRepository appointmentBookingRepository,
            AppointmentRepository appointmentRepository,
            SessionManager sessionManager,
            EventManager eventManager
    ) {
        this(appointmentBookingRepository, appointmentRepository, sessionManager, eventManager, new SystemTimeProvider());
    }

    /**
     * Creates a user appointment service with time provider injection.
     *
     * @param appointmentBookingRepository repository for appointment persistence
     * @param appointmentRepository repository for slot management
     * @param sessionManager session manager for user identification
     * @param eventManager event manager for notifications
     * @param timeProvider provider for current time
     */
    public UserAppointmentService(
            AppointmentBookingRepository appointmentBookingRepository,
            AppointmentRepository appointmentRepository,
            SessionManager sessionManager,
            EventManager eventManager,
            TimeProvider timeProvider
    ) {
        this.appointmentBookingRepository = Objects.requireNonNull(
                appointmentBookingRepository,
                "appointmentBookingRepository cannot be null"
        );
        this.appointmentRepository = Objects.requireNonNull(
                appointmentRepository,
                "appointmentRepository cannot be null"
        );
        this.sessionManager = Objects.requireNonNull(sessionManager, "sessionManager cannot be null");
        this.eventManager = Objects.requireNonNull(eventManager, "eventManager cannot be null");
        this.timeProvider = Objects.requireNonNull(timeProvider, "timeProvider cannot be null");
    }

    /**
     * Returns all appointments for the current logged-in user.
     * USER must be logged in; ADMIN cannot use this flow (use AdminReservationService instead).
     *
     * @return list of appointments for current user
     * @throws IllegalStateException if user is not logged in or is an ADMIN
     */
    public List<Appointment> getMyAppointments() {
        if (!sessionManager.isLoggedIn()) {
            throw new IllegalStateException("user must be logged in to view their appointments");
        }
        if (sessionManager.isAdmin()) {
            throw new IllegalStateException("ADMIN should use AdminReservationService instead");
        }

        String username = sessionManager.getCurrentUsername();
        return appointmentBookingRepository.findByCustomerName(username);
    }

    /**
     * Finds a specific appointment by id, verifying the current user owns it.
     *
     * @param appointmentId the appointment identifier
     * @return optional appointment if found and owned by current user
     * @throws IllegalStateException if user is not logged in or is an ADMIN
     */
    public Optional<Appointment> getMyAppointment(String appointmentId) {
        if (!sessionManager.isLoggedIn()) {
            throw new IllegalStateException("user must be logged in to view their appointments");
        }
        if (sessionManager.isAdmin()) {
            throw new IllegalStateException("ADMIN should use AdminReservationService instead");
        }

        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            return Optional.empty();
        }

        Optional<Appointment> found = appointmentBookingRepository.findById(appointmentId.trim());
        String username = sessionManager.getCurrentUsername();

        // Only return if it belongs to the current user
        if (found.isPresent() && username.equals(found.get().getCustomerName())) {
            return found;
        }

        return Optional.empty();
    }

    /**
     * Modifies an existing appointment (time or duration).
     * USER can only modify their own appointments.
     * Modifications are only allowed for FUTURE appointments (start time is in the future).
     *
     * @param appointmentId appointment to modify
     * @param newStartTime new start time (or null to keep current)
     * @param newDurationMinutes new duration in minutes (or null to keep current)
     * @return true if modification was successful
     * @throws IllegalStateException if user is not logged in, is an ADMIN, or appointment not found
     * @throws IllegalArgumentException if trying to modify a past appointment
     */
    public boolean modifyAppointment(String appointmentId, LocalDateTime newStartTime, Integer newDurationMinutes) {
        if (!sessionManager.isLoggedIn()) {
            throw new IllegalStateException("user must be logged in to modify appointments");
        }
        if (sessionManager.isAdmin()) {
            throw new IllegalStateException("ADMIN should use AdminReservationService instead");
        }

        Optional<Appointment> optionalAppointment = getMyAppointment(appointmentId);
        if (optionalAppointment.isEmpty()) {
            throw new IllegalStateException("appointment not found or does not belong to user");
        }

        Appointment current = optionalAppointment.get();

        // Check if appointment is in the future
        LocalDateTime now = timeProvider.now();
        if (current.getStartTime().isBefore(now)) {
            throw new IllegalArgumentException("cannot modify past appointments");
        }

        // Create modified copy
        LocalDateTime finalStartTime = newStartTime != null ? newStartTime : current.getStartTime();
        int finalDuration = newDurationMinutes != null ? newDurationMinutes : current.getDurationMinutes();

        Appointment modified = new Appointment(
                current.getId(),
                current.getCustomerName(),
                finalStartTime,
                finalDuration,
                current.getParticipantCount(),
                current.getStatus()
        );

        boolean updated = appointmentBookingRepository.update(modified);
        if (updated) {
            String username = sessionManager.getCurrentUsername();
            eventManager.notifyObservers(
                    "Appointment " + appointmentId + " modified by " + username
            );
        }
        return updated;
    }

    /**
     * Cancels an existing appointment.
     * USER can only cancel their own appointments.
     * Cancellations are only allowed for FUTURE appointments (start time is in the future).
     *
     * @param appointmentId appointment to cancel
     * @return true if cancellation was successful
     * @throws IllegalStateException if user is not logged in, is an ADMIN, or appointment not found
     * @throws IllegalArgumentException if trying to cancel a past appointment
     */
    public boolean cancelAppointment(String appointmentId) {
        if (!sessionManager.isLoggedIn()) {
            throw new IllegalStateException("user must be logged in to cancel appointments");
        }
        if (sessionManager.isAdmin()) {
            throw new IllegalStateException("ADMIN should use AdminReservationService instead");
        }

        Optional<Appointment> optionalAppointment = getMyAppointment(appointmentId);
        if (optionalAppointment.isEmpty()) {
            throw new IllegalStateException("appointment not found or does not belong to user");
        }

        Appointment current = optionalAppointment.get();

        // Check if appointment is in the future
        LocalDateTime now = timeProvider.now();
        if (current.getStartTime().isBefore(now)) {
            throw new IllegalArgumentException("cannot cancel past appointments");
        }

        // Mark as cancelled
        Appointment cancelled = new Appointment(
                current.getId(),
                current.getCustomerName(),
                current.getStartTime(),
                current.getDurationMinutes(),
                current.getParticipantCount(),
                AppointmentStatus.CANCELLED
        );

        boolean updated = appointmentBookingRepository.update(cancelled);
        if (updated) {
            String username = sessionManager.getCurrentUsername();
            eventManager.notifyObservers(
                    "Appointment " + appointmentId + " cancelled by " + username
            );

            // Release the slot back to available (only if it was booked by this user)
            // This allows other users to book the same slot
            // (In a real system, you might notify admins about the cancellation)
        }
        return updated;
    }

    /**
     * Returns future appointments for the current user.
     *
     * @return list of future appointments
     * @throws IllegalStateException if user is not logged in or is an ADMIN
     */
    public List<Appointment> getMyFutureAppointments() {
        if (!sessionManager.isLoggedIn()) {
            throw new IllegalStateException("user must be logged in to view their appointments");
        }
        if (sessionManager.isAdmin()) {
            throw new IllegalStateException("ADMIN should use AdminReservationService instead");
        }

        LocalDateTime now = timeProvider.now();
        List<Appointment> future = new ArrayList<>();

        for (Appointment appointment : getMyAppointments()) {
            if (appointment.getStartTime().isAfter(now)) {
                future.add(appointment);
            }
        }

        return future;
    }

    /**
     * Returns past appointments for the current user.
     *
     * @return list of past appointments
     * @throws IllegalStateException if user is not logged in or is an ADMIN
     */
    public List<Appointment> getMyPastAppointments() {
        if (!sessionManager.isLoggedIn()) {
            throw new IllegalStateException("user must be logged in to view their appointments");
        }
        if (sessionManager.isAdmin()) {
            throw new IllegalStateException("ADMIN should use AdminReservationService instead");
        }

        LocalDateTime now = timeProvider.now();
        List<Appointment> past = new ArrayList<>();

        for (Appointment appointment : getMyAppointments()) {
            if (appointment.getStartTime().isBefore(now)) {
                past.add(appointment);
            }
        }

        return past;
    }
}

