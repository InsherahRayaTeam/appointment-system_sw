package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentSlot;
import org.example.domain.AppointmentStatus;
import org.example.domain.UserRole;
import org.example.repository.AppointmentBookingRepository;
import org.example.repository.AppointmentRepository;
import org.example.repository.UserRepository;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Handles booking workflow validation and persistence for customer appointments.
 *
 * @author appointment-system
 * @version 1.0
 */
public class AppointmentBookingService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentBookingRepository appointmentBookingRepository;
    private final BookingRuleStrategy durationRule;
    private final BookingRuleStrategy participantRule;
    private final SessionManager sessionManager;
    private final UserRepository userRepository;
    private final EventManager eventManager;

    /**
     * Creates a booking service using repository dependencies.
     *
     * @param appointmentRepository repository used to read and update slot availability
     * @param appointmentBookingRepository repository used to store confirmed appointments
     */
    public AppointmentBookingService(
            AppointmentRepository appointmentRepository,
            AppointmentBookingRepository appointmentBookingRepository
    ) {
        this(appointmentRepository, appointmentBookingRepository, null, null, null);
    }

    /**
     * Creates a booking service with optional management/auth dependencies.
     *
     * @param appointmentRepository repository used to read and update slot availability
     * @param appointmentBookingRepository repository used to store confirmed appointments
     * @param sessionManager session manager used to enforce authenticated admin-only management
     * @param userRepository repository used to validate user identity when needed
     * @param eventManager event manager used for reservation-management notifications
     */
    public AppointmentBookingService(
            AppointmentRepository appointmentRepository,
            AppointmentBookingRepository appointmentBookingRepository,
            SessionManager sessionManager,
            UserRepository userRepository,
            EventManager eventManager
    ) {
        this.appointmentRepository = Objects.requireNonNull(
                appointmentRepository,
                "appointmentRepository cannot be null"
        );
        this.appointmentBookingRepository = Objects.requireNonNull(
                appointmentBookingRepository,
                "appointmentBookingRepository cannot be null"
        );
        this.durationRule = new DurationRule();
        this.participantRule = new ParticipantRule();
        this.sessionManager = sessionManager;
        this.userRepository = userRepository;
        this.eventManager = eventManager;
    }

    /**
     * Attempts to book an appointment using raw string input values.
     * This keeps request validation and conversion in the service layer.
     *
     * @param customerName the customer name for the booking
     * @param slotTime the requested slot time label
     * @param durationMinutesInput the requested duration input in minutes
     * @param participantCountInput the requested participant count input
     * @return the booking outcome status
     */
    public BookingStatus bookAppointment(
            String customerName,
            String slotTime,
            String durationMinutesInput,
            String participantCountInput
    ) {
        if (customerName == null || customerName.trim().isEmpty()) {
            return BookingStatus.BLANK_CUSTOMER_NAME;
        }
        if (slotTime == null || slotTime.trim().isEmpty()) {
            return BookingStatus.BLANK_SLOT_TIME;
        }

        Integer durationMinutes = parseInteger(durationMinutesInput);
        if (durationMinutes == null) {
            return BookingStatus.INVALID_DURATION;
        }

        Integer participantCount = parseInteger(participantCountInput);
        if (participantCount == null) {
            return BookingStatus.INVALID_PARTICIPANT_COUNT;
        }

        return bookAppointment(customerName, slotTime, durationMinutes, participantCount);
    }

    /**
     * Attempts to book an appointment using the provided request details.
     * The service validates request constraints, checks slot availability,
     * books the slot, and persists a confirmed appointment on success.
     *
     * @param customerName the customer name for the booking
     * @param slotTime the requested slot time label
     * @param durationMinutes the requested duration in minutes
     * @param participantCount the number of participants for the booking
     * @return the booking outcome status
     */
    public BookingStatus bookAppointment(
            String customerName,
            String slotTime,
            int durationMinutes,
            int participantCount
    ) {
        if (customerName == null || customerName.trim().isEmpty()) {
            return BookingStatus.BLANK_CUSTOMER_NAME;
        }
        if (slotTime == null || slotTime.trim().isEmpty()) {
            return BookingStatus.BLANK_SLOT_TIME;
        }

        Appointment probe = validationProbe(durationMinutes, participantCount);
        if (!durationRule.isValid(probe)) {
            return BookingStatus.INVALID_DURATION;
        }
        if (!participantRule.isValid(probe)) {
            return BookingStatus.INVALID_PARTICIPANT_COUNT;
        }

        String normalizedCustomerName = customerName.trim();
        String normalizedSlotTime = slotTime.trim();
        boolean matchingSlotFound = false;

        for (AppointmentSlot slot : appointmentRepository.findAll()) {
            if (slot.getTime().equals(normalizedSlotTime)) {
                if (!slot.isAvailable()) {
                    matchingSlotFound = true;
                    continue;
                }

                slot.book();
                appointmentBookingRepository.save(new Appointment(
                        normalizedCustomerName,
                        normalizedSlotTime,
                        durationMinutes,
                        participantCount,
                        AppointmentStatus.CONFIRMED
                ));
                return BookingStatus.SUCCESS;
            }
        }

        if (matchingSlotFound) {
            return BookingStatus.SLOT_ALREADY_BOOKED;
        }

        return BookingStatus.SLOT_NOT_FOUND;
    }

    /**
     * Returns all reservations when current session is an authenticated administrator.
     *
     * @return managed reservation list, or empty list when access is not allowed
     */
    public List<Appointment> getManagedReservations() {
        if (!isCurrentUserAdmin()) {
            return Collections.emptyList();
        }
        return appointmentBookingRepository.findAll();
    }

    /**
     * Indicates whether the current session is authorized to manage reservations.
     *
     * @return true when a logged-in administrator is present
     */
    public boolean canCurrentUserManageReservations() {
        return isCurrentUserAdmin();
    }

    /**
     * Returns reservations for a specific customer email when session access is valid.
     *
     * Access rules:
     * - Admin can request any customer's reservations.
     * - Regular users can request only their own reservations.
     *
     * @param customerEmail customer email used during booking
     * @return matching reservations, or empty list when input/access is invalid
     */
    public List<Appointment> getReservationsForCustomer(String customerEmail) {
        String normalizedCustomerEmail = normalize(customerEmail);
        if (normalizedCustomerEmail == null || sessionManager == null || !sessionManager.isLoggedIn()) {
            return Collections.emptyList();
        }

        String currentEmail = normalize(sessionManager.getCurrentEmail());
        boolean isAdmin = sessionManager.isAdmin();

        if (!isAdmin && (currentEmail == null || !normalizedCustomerEmail.equalsIgnoreCase(currentEmail))) {
            return Collections.emptyList();
        }

        List<Appointment> results = new ArrayList<>();
        for (Appointment appointment : appointmentBookingRepository.findAll()) {
            String bookingCustomer = normalize(appointment.getCustomerName());
            if (bookingCustomer != null && normalizedCustomerEmail.equalsIgnoreCase(bookingCustomer)) {
                results.add(appointment);
            }
        }
        return results;
    }

    /**
     * Cancels an existing reservation.
     *
     * @param appointmentId reservation identifier
     * @return operation status
     */
    public BookingStatus cancelAppointment(String appointmentId) {
        if (!isCurrentUserAdmin()) {
            return BookingStatus.UNAUTHORIZED;
        }

        Appointment appointment = appointmentBookingRepository.findById(normalize(appointmentId)).orElse(null);
        if (appointment == null) {
            return BookingStatus.APPOINTMENT_NOT_FOUND;
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return BookingStatus.APPOINTMENT_ALREADY_CANCELLED;
        }
        if (!appointment.isFutureComparedTo(LocalDateTime.now())) {
            return BookingStatus.APPOINTMENT_NOT_FUTURE;
        }

        AppointmentSlot slot = findSlotByTime(appointment.getSlotTime());
        if (slot != null) {
            slot.release();
        }

        Appointment updated = appointment.withStatus(AppointmentStatus.CANCELLED);
        if (!appointmentBookingRepository.update(updated)) {
            if (slot != null) {
                slot.book();
            }
            return BookingStatus.UPDATE_FAILED;
        }

        notifyEvent("Reservation cancelled: " + updated.getId());
        return BookingStatus.SUCCESS;
    }

    /**
     * Modifies a reservation by reassigning it to a different available slot.
     *
     * @param appointmentId reservation identifier
     * @param newSlotTime requested replacement slot time
     * @return operation status
     */
    public BookingStatus modifyAppointment(String appointmentId, String newSlotTime) {
        if (!isCurrentUserAdmin()) {
            return BookingStatus.UNAUTHORIZED;
        }
        if (newSlotTime == null || newSlotTime.trim().isEmpty()) {
            return BookingStatus.BLANK_SLOT_TIME;
        }

        Appointment appointment = appointmentBookingRepository.findById(normalize(appointmentId)).orElse(null);
        if (appointment == null) {
            return BookingStatus.APPOINTMENT_NOT_FOUND;
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return BookingStatus.APPOINTMENT_ALREADY_CANCELLED;
        }
        if (!appointment.isFutureComparedTo(LocalDateTime.now())) {
            return BookingStatus.APPOINTMENT_NOT_FUTURE;
        }

        String normalizedNewSlotTime = newSlotTime.trim();
        AppointmentSlot targetSlot = findSlotByTime(normalizedNewSlotTime);
        if (targetSlot == null) {
            return BookingStatus.SLOT_NOT_FOUND;
        }
        if (!targetSlot.isAvailable()) {
            return BookingStatus.SLOT_ALREADY_BOOKED;
        }

        AppointmentSlot currentSlot = findSlotByTime(appointment.getSlotTime());
        if (currentSlot != null) {
            currentSlot.release();
        }

        targetSlot.book();
        Appointment updated = appointment.withSlotTimeAndStatus(normalizedNewSlotTime, AppointmentStatus.MODIFIED);
        if (!appointmentBookingRepository.update(updated)) {
            targetSlot.release();
            if (currentSlot != null) {
                currentSlot.book();
            }
            return BookingStatus.UPDATE_FAILED;
        }

        notifyEvent("Reservation modified: " + updated.getId() + " -> " + normalizedNewSlotTime);
        return BookingStatus.SUCCESS;
    }

    private AppointmentSlot findSlotByTime(String slotTime) {
        String normalized = normalize(slotTime);
        if (normalized == null) {
            return null;
        }

        for (AppointmentSlot slot : appointmentRepository.findAll()) {
            if (normalized.equals(slot.getTime())) {
                return slot;
            }
        }
        return null;
    }

    /**
     * Checks whether the current logged-in session belongs to an administrator.
     *
     * Uses SessionManager directly as the source of truth.
     *
     * @return true if the logged-in user is an admin, otherwise false
     */
    private boolean isCurrentUserAdmin() {
        if (sessionManager == null || !sessionManager.isLoggedIn()) {
            return false;
        }

        if (sessionManager.isAdmin()) {
            return true;
        }

        if (userRepository == null) {
            return false;
        }

        String email = normalize(sessionManager.getCurrentEmail());
        return email != null && userRepository.findByEmail(email)
                .map(user -> user.getRole() == UserRole.ADMIN)
                .orElse(false);
    }

    private void notifyEvent(String message) {
        if (eventManager != null && message != null && !message.trim().isEmpty()) {
            eventManager.notifyObservers(message);
        }
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private Integer parseInteger(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Appointment validationProbe(int durationMinutes, int participantCount) {
        return new Appointment("validation", LocalDateTime.now(), durationMinutes, participantCount);
    }
}