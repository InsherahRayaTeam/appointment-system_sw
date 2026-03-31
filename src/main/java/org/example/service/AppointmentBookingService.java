package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentSlot;
import org.example.domain.AppointmentStatus;
import org.example.domain.UserRole;
import org.example.repository.AdminRepository;
import org.example.repository.AppointmentBookingRepository;
import org.example.repository.AppointmentRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private final AdminRepository adminRepository;
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
     * @param adminRepository repository used to validate administrator identity
     * @param eventManager event manager used for reservation-management notifications
     */
    public AppointmentBookingService(
            AppointmentRepository appointmentRepository,
            AppointmentBookingRepository appointmentBookingRepository,
            SessionManager sessionManager,
            AdminRepository adminRepository,
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
        this.adminRepository = adminRepository;
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
        if (isCurrentUserAdmin()) {
            return BookingStatus.UNAUTHORIZED;
        }

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
     * Returns reservations owned by the currently logged-in regular user.
     *
     * @return current user's reservations, or empty list when session is not a regular user
     */
    public List<Appointment> getCurrentUserReservations() {
        if (!isCurrentUserRegularUser()) {
            return Collections.emptyList();
        }

        String username = normalize(sessionManager.getCurrentUsername());
        if (username == null) {
            return Collections.emptyList();
        }

        return appointmentBookingRepository.findAll().stream()
                .filter(appointment -> username.equals(normalize(appointment.getCustomerName())))
                .collect(Collectors.toList());
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
     * Cancels an existing reservation.
     *
     * @param appointmentId reservation identifier
     * @return operation status
     */
    public BookingStatus cancelAppointment(String appointmentId) {
        return cancelManagedReservation(appointmentId);
    }

    /**
     * Cancels an upcoming appointment without admin-role checks.
     *
     * @param appointmentId appointment identifier
     * @return operation status
     */
    public BookingStatus cancelUpcomingAppointment(String appointmentId) {
        return cancelCore(appointmentId);
    }

    /**
     * Cancels an existing reservation as an administrator-managed action.
     *
     * @param appointmentId reservation identifier
     * @return operation status
     */
    public BookingStatus cancelManagedReservation(String appointmentId) {
        if (!isCurrentUserAdmin()) {
            return BookingStatus.UNAUTHORIZED;
        }

        return cancelCore(appointmentId);
    }

    /**
     * Modifies an existing appointment without admin-role checks.
     *
     * @param appointmentId appointment identifier
     * @param newSlotTime requested replacement slot time
     * @return operation status
     */
    public BookingStatus modifyUpcomingAppointment(String appointmentId, String newSlotTime) {
        if (!isCurrentUserRegularUser()) {
            return BookingStatus.UNAUTHORIZED;
        }

        Appointment appointment = findReservationById(appointmentId);
        if (appointment == null || !isOwnedByCurrentUser(appointment)) {
            return BookingStatus.APPOINTMENT_NOT_FOUND;
        }

        return modifyCore(appointmentId, newSlotTime);
    }

    /**
     * Cancels an appointment owned by the currently logged-in regular user.
     *
     * @param appointmentId appointment identifier
     * @return operation status
     */
    public BookingStatus cancelMyAppointment(String appointmentId) {
        if (!isCurrentUserRegularUser()) {
            return BookingStatus.UNAUTHORIZED;
        }

        Appointment appointment = findReservationById(appointmentId);
        if (appointment == null || !isOwnedByCurrentUser(appointment)) {
            return BookingStatus.APPOINTMENT_NOT_FOUND;
        }

        return cancelCore(appointmentId);
    }

    /**
     * Modifies an appointment owned by the currently logged-in regular user.
     *
     * @param appointmentId appointment identifier
     * @param newSlotTime requested replacement slot time
     * @return operation status
     */
    public BookingStatus modifyMyAppointment(String appointmentId, String newSlotTime) {
        return modifyUpcomingAppointment(appointmentId, newSlotTime);
    }

    /**
     * Modifies a reservation as an administrator-managed action.
     *
     * @param appointmentId reservation identifier
     * @param newSlotTime requested replacement slot time
     * @return operation status
     */
    public BookingStatus modifyManagedReservation(String appointmentId, String newSlotTime) {
        if (!isCurrentUserAdmin()) {
            return BookingStatus.UNAUTHORIZED;
        }
        return modifyCore(appointmentId, newSlotTime);
    }

    private BookingStatus cancelCore(String appointmentId) {
        String normalizedAppointmentId = normalize(appointmentId);
        if (normalizedAppointmentId == null) {
            return BookingStatus.APPOINTMENT_NOT_FOUND;
        }

        Appointment appointment = appointmentBookingRepository.findById(normalizedAppointmentId).orElse(null);
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
        return modifyManagedReservation(appointmentId, newSlotTime);
    }

    private BookingStatus modifyCore(String appointmentId, String newSlotTime) {
        String normalizedAppointmentId = normalize(appointmentId);
        if (normalizedAppointmentId == null) {
            return BookingStatus.APPOINTMENT_NOT_FOUND;
        }

        String normalizedNewSlotTime = normalize(newSlotTime);
        if (normalizedNewSlotTime == null) {
            return BookingStatus.BLANK_SLOT_TIME;
        }

        Appointment appointment = appointmentBookingRepository.findById(normalizedAppointmentId).orElse(null);
        if (appointment == null) {
            return BookingStatus.APPOINTMENT_NOT_FOUND;
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return BookingStatus.APPOINTMENT_ALREADY_CANCELLED;
        }
        if (!appointment.isFutureComparedTo(LocalDateTime.now())) {
            return BookingStatus.APPOINTMENT_NOT_FUTURE;
        }

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
     * Adds a new appointment slot (admin-only operation).
     *
     * @param slotTime slot time identifier to add
     * @return booking status indicating success or failure
     */
    public BookingStatus addManagedSlot(String slotTime) {
        if (!isCurrentUserAdmin()) {
            return BookingStatus.UNAUTHORIZED;
        }

        if (slotTime == null || slotTime.trim().isEmpty()) {
            return BookingStatus.BLANK_SLOT_TIME;
        }

        String normalizedSlotTime = slotTime.trim();
        boolean added = appointmentRepository.findByTime(normalizedSlotTime).isPresent();
        if (added) {
            return BookingStatus.SLOT_ALREADY_BOOKED;  // Reusing existing status for "slot already exists"
        }

        AppointmentSlot newSlot = new AppointmentSlot(normalizedSlotTime);
        if (appointmentRepository.save(newSlot)) {
            notifyEvent("Slot added: " + normalizedSlotTime);
            return BookingStatus.SUCCESS;
        }

        return BookingStatus.SLOT_NOT_FOUND;
    }

    /**
     * Cancels an appointment slot (admin-only operation).
     * A cancelled slot cannot be booked by users.
     *
     * @param slotTime slot time identifier to cancel
     * @return booking status indicating success or failure
     */
    public BookingStatus cancelManagedSlot(String slotTime) {
        if (!isCurrentUserAdmin()) {
            return BookingStatus.UNAUTHORIZED;
        }

        if (slotTime == null || slotTime.trim().isEmpty()) {
            return BookingStatus.BLANK_SLOT_TIME;
        }

        String normalizedSlotTime = slotTime.trim();
        if (appointmentRepository.removeSlot(normalizedSlotTime)) {
            notifyEvent("Slot cancelled: " + normalizedSlotTime);
            return BookingStatus.SUCCESS;
        }

        return BookingStatus.SLOT_NOT_FOUND;
    }

    private Appointment findReservationById(String appointmentId) {
        String normalizedAppointmentId = normalize(appointmentId);
        if (normalizedAppointmentId == null) {
            return null;
        }
        return appointmentBookingRepository.findById(normalizedAppointmentId).orElse(null);
    }

    private boolean isCurrentUserAdmin() {
        if (sessionManager == null || !sessionManager.isLoggedIn()) {
            return false;
        }

        if (sessionManager.isAdmin()) {
            return true;
        }

        if (adminRepository == null) {
            return false;
        }
        String username = normalize(sessionManager.getCurrentUsername());
        return username != null && adminRepository.findByUsername(username)
                .map(user -> user.getRole() == org.example.domain.UserRole.ADMIN)
                .orElse(false);
    }

    private boolean isCurrentUserRegularUser() {
        if (sessionManager == null || !sessionManager.isLoggedIn()) {
            return false;
        }

        UserRole role = sessionManager.getCurrentUserRole();
        if (role != null) {
            return role == UserRole.USER;
        }

        if (sessionManager.isAdmin()) {
            return false;
        }

        if (adminRepository == null) {
            return true;
        }

        String username = normalize(sessionManager.getCurrentUsername());
        return username != null && adminRepository.findByUsername(username)
                .map(user -> user.getRole() == UserRole.USER)
                .orElse(false);
    }

    private boolean isOwnedByCurrentUser(Appointment appointment) {
        if (appointment == null || sessionManager == null) {
            return false;
        }
        String currentUsername = normalize(sessionManager.getCurrentUsername());
        String owner = normalize(appointment.getCustomerName());
        return currentUsername != null && currentUsername.equals(owner);
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
