package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentStatus;
import org.example.repository.AppointmentBookingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service for admin-only reservation management operations.
 * Handles ADMIN-only operations like viewing, managing, and cancelling customer reservations.
 *
 * ADMIN operations:
 * - View all reservations
 * - View reservations by customer
 * - Cancel customer reservations
 * - Get reservation statistics
 *
 * ADMIN CANNOT:
 * - Book appointments for themselves (use the customer booking flow instead)
 *
 * @author appointment-system
 * @version 1.0
 */
public class AdminReservationService {

    private final AppointmentBookingRepository appointmentBookingRepository;
    private final SessionManager sessionManager;
    private final EventManager eventManager;

    /**
     * Creates an admin reservation service.
     *
     * @param appointmentBookingRepository repository for appointment persistence
     * @param sessionManager session manager for role verification
     * @param eventManager event manager for notifications
     */
    public AdminReservationService(
            AppointmentBookingRepository appointmentBookingRepository,
            SessionManager sessionManager,
            EventManager eventManager
    ) {
        this.appointmentBookingRepository = Objects.requireNonNull(
                appointmentBookingRepository,
                "appointmentBookingRepository cannot be null"
        );
        this.sessionManager = Objects.requireNonNull(sessionManager, "sessionManager cannot be null");
        this.eventManager = Objects.requireNonNull(eventManager, "eventManager cannot be null");
    }

    /**
     * Returns all reservations in the system.
     * ADMIN-only operation.
     *
     * @return list of all appointments
     * @throws IllegalStateException if user is not logged in or not an ADMIN
     */
    public List<Appointment> getAllReservations() {
        if (!sessionManager.isLoggedIn()) {
            throw new IllegalStateException("user must be logged in");
        }
        if (!sessionManager.isAdmin()) {
            throw new IllegalStateException("only ADMIN role can view all reservations");
        }

        return appointmentBookingRepository.findAll();
    }

    /**
     * Returns all reservations for a specific customer.
     * ADMIN-only operation.
     *
     * @param customerName the customer name
     * @return list of appointments for this customer
     * @throws IllegalStateException if user is not logged in or not an ADMIN
     */
    public List<Appointment> getReservationsByCustomer(String customerName) {
        if (!sessionManager.isLoggedIn()) {
            throw new IllegalStateException("user must be logged in");
        }
        if (!sessionManager.isAdmin()) {
            throw new IllegalStateException("only ADMIN role can view customer reservations");
        }

        if (customerName == null || customerName.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return appointmentBookingRepository.findByCustomerName(customerName.trim());
    }

    /**
     * Finds a specific reservation by id.
     * ADMIN-only operation.
     *
     * @param appointmentId the appointment identifier
     * @return optional appointment if found
     * @throws IllegalStateException if user is not logged in or not an ADMIN
     */
    public Optional<Appointment> getReservation(String appointmentId) {
        if (!sessionManager.isLoggedIn()) {
            throw new IllegalStateException("user must be logged in");
        }
        if (!sessionManager.isAdmin()) {
            throw new IllegalStateException("only ADMIN role can view reservations");
        }

        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            return Optional.empty();
        }

        return appointmentBookingRepository.findById(appointmentId.trim());
    }

    /**
     * Cancels a reservation (customer appointment).
     * ADMIN-only operation.
     *
     * @param appointmentId the appointment to cancel
     * @param reason reason for cancellation
     * @return true if cancellation was successful
     * @throws IllegalStateException if user is not logged in or not an ADMIN
     */
    public boolean cancelReservation(String appointmentId, String reason) {
        if (!sessionManager.isLoggedIn()) {
            throw new IllegalStateException("user must be logged in");
        }
        if (!sessionManager.isAdmin()) {
            throw new IllegalStateException("only ADMIN role can cancel reservations");
        }

        Optional<Appointment> optionalAppointment = appointmentBookingRepository.findById(appointmentId);
        if (optionalAppointment.isEmpty()) {
            return false;
        }

        Appointment current = optionalAppointment.get();

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
            String adminUsername = sessionManager.getCurrentUsername();
            String reasonDisplay = reason != null && !reason.isEmpty() ? reason : "no reason specified";
            eventManager.notifyObservers(
                    "Reservation " + appointmentId + " cancelled by ADMIN " + adminUsername + " - reason: " + reasonDisplay
            );
        }
        return updated;
    }

    /**
     * Gets statistics about all reservations.
     * ADMIN-only operation.
     *
     * @return object containing reservation statistics
     * @throws IllegalStateException if user is not logged in or not an ADMIN
     */
    public ReservationStats getReservationStats() {
        if (!sessionManager.isLoggedIn()) {
            throw new IllegalStateException("user must be logged in");
        }
        if (!sessionManager.isAdmin()) {
            throw new IllegalStateException("only ADMIN role can view statistics");
        }

        List<Appointment> all = appointmentBookingRepository.findAll();
        int total = all.size();
        int active = 0;
        int cancelled = 0;

        for (Appointment appointment : all) {
            if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
                cancelled++;
            } else {
                active++;
            }
        }

        return new ReservationStats(total, active, cancelled);
    }

    /**
     * DTO for reservation statistics.
     */
    public static class ReservationStats {
        private final int totalReservations;
        private final int activeReservations;
        private final int cancelledReservations;

        public ReservationStats(int totalReservations, int activeReservations, int cancelledReservations) {
            this.totalReservations = totalReservations;
            this.activeReservations = activeReservations;
            this.cancelledReservations = cancelledReservations;
        }

        public int getTotalReservations() {
            return totalReservations;
        }

        public int getActiveReservations() {
            return activeReservations;
        }

        public int getCancelledReservations() {
            return cancelledReservations;
        }

        @Override
        public String toString() {
            return "ReservationStats{" +
                    "totalReservations=" + totalReservations +
                    ", activeReservations=" + activeReservations +
                    ", cancelledReservations=" + cancelledReservations +
                    '}';
        }
    }
}

