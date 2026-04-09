package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentSlot;
import org.example.domain.AppointmentStatus;
import org.example.domain.AppointmentType;
import org.example.domain.UserRole;
import org.example.repository.AppointmentBookingRepository;
import org.example.repository.AppointmentRepository;
import org.example.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents appointment booking service in the system.
 */
public class AppointmentBookingService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentBookingRepository appointmentBookingRepository;
    private final BookingRuleStrategy durationRule;
    private final BookingRuleStrategy participantRule;
    private final Map<AppointmentType, AppointmentTypeRule> appointmentTypeRules;
    private final SessionManager sessionManager;
    private final UserRepository userRepository;
    private final EventManager eventManager;
    private final AppointmentNotificationCoordinator appointmentNotificationCoordinator;

    /**
     * Creates a new appointment booking service object with the given values.
     *
     * @param appointmentRepository repository used to read and save data
     * @param appointmentBookingRepository repository used to read and save data
     */
    public AppointmentBookingService(
            AppointmentRepository appointmentRepository,
            AppointmentBookingRepository appointmentBookingRepository
    ) {
        this(appointmentRepository, appointmentBookingRepository, null, null, null);
    }

    /**
     * Creates a new appointment booking service object with the given values.
     *
     * @param appointmentRepository repository used to read and save data
     * @param appointmentBookingRepository repository used to read and save data
     * @param sessionManager manager object used for shared app state
     * @param userRepository user involved in this action
     * @param eventManager manager object used for shared app state
     */
    public AppointmentBookingService(
            AppointmentRepository appointmentRepository,
            AppointmentBookingRepository appointmentBookingRepository,
            SessionManager sessionManager,
            UserRepository userRepository,
            EventManager eventManager
    ) {
        this(
                appointmentRepository,
                appointmentBookingRepository,
                sessionManager,
                userRepository,
                eventManager,
                null
        );
    }

    /**
     * Creates a new appointment booking service object with the given values.
     *
     * @param appointmentRepository repository used to read and save data
     * @param appointmentBookingRepository repository used to read and save data
     * @param sessionManager manager object used for shared app state
     * @param userRepository user involved in this action
     * @param eventManager manager object used for shared app state
     * @param appointmentNotificationCoordinator coordinator used to send appointment notifications
     */
    public AppointmentBookingService(
            AppointmentRepository appointmentRepository,
            AppointmentBookingRepository appointmentBookingRepository,
            SessionManager sessionManager,
            UserRepository userRepository,
            EventManager eventManager,
            AppointmentNotificationCoordinator appointmentNotificationCoordinator
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
        this.appointmentTypeRules = buildTypeRuleMap(List.of(
                new NormalRule(),
                new UrgentRule(),
                new FollowUpRule(),
                new AssessmentRule(),
                new VirtualRule(),
                new InPersonRule(),
                new IndividualRule(),
                new GroupRule()
        ));
        this.sessionManager = sessionManager;
        this.userRepository = userRepository;
        this.eventManager = eventManager;
        this.appointmentNotificationCoordinator = appointmentNotificationCoordinator;
    }

    /**
     * Books appointment when allowed.
     *
     * @param customerName value for customer name
     * @param slotTime slot time text like 10:00
     * @param durationMinutesInput appointment duration in minutes
     * @param participantCountInput number of people for the appointment
     * @return status that explains the operation result
     */
    public BookingStatus bookAppointment(
            String customerName,
            String slotTime,
            String durationMinutesInput,
            String participantCountInput
    ) {
        return bookAppointment(
                customerName,
                slotTime,
                durationMinutesInput,
                participantCountInput,
                AppointmentType.NORMAL
        );
    }

    /**
     * Books appointment when allowed.
     *
     * @param customerName value for customer name
     * @param slotTime slot time text like 10:00
     * @param durationMinutesInput appointment duration in minutes
     * @param participantCountInput number of people for the appointment
     * @param appointmentType value for appointment type
     * @return status that explains the operation result
     */
    public BookingStatus bookAppointment(
            String customerName,
            String slotTime,
            String durationMinutesInput,
            String participantCountInput,
            AppointmentType appointmentType
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

        return bookAppointment(customerName, slotTime, durationMinutes, participantCount, appointmentType);
    }

    /**
     * Books appointment when allowed.
     *
     * @param customerName value for customer name
     * @param slotTime slot time text like 10:00
     * @param durationMinutes appointment duration in minutes
     * @param participantCount number of people for the appointment
     * @return status that explains the operation result
     */
    public BookingStatus bookAppointment(
            String customerName,
            String slotTime,
            int durationMinutes,
            int participantCount
    ) {
        return bookAppointment(customerName, slotTime, durationMinutes, participantCount, AppointmentType.NORMAL);
    }

    /**
     * Books appointment when allowed.
     *
     * @param customerName value for customer name
     * @param slotTime slot time text like 10:00
     * @param durationMinutes appointment duration in minutes
     * @param participantCount number of people for the appointment
     * @param appointmentType value for appointment type
     * @return status that explains the operation result
     */
    public BookingStatus bookAppointment(
            String customerName,
            String slotTime,
            int durationMinutes,
            int participantCount,
            AppointmentType appointmentType
    ) {
        if (customerName == null || customerName.trim().isEmpty()) {
            return BookingStatus.BLANK_CUSTOMER_NAME;
        }
        if (slotTime == null || slotTime.trim().isEmpty()) {
            return BookingStatus.BLANK_SLOT_TIME;
        }

        Appointment probe = validationProbe(durationMinutes, participantCount);
        AppointmentType normalizedType = normalizeType(appointmentType);
        probe.setType(normalizedType);

        if (!durationRule.isValid(probe)) {
            return BookingStatus.INVALID_DURATION;
        }
        if (!participantRule.isValid(probe)) {
            return BookingStatus.INVALID_PARTICIPANT_COUNT;
        }
        if (!typeRulesAllow(probe, normalizedType)) {
            return BookingStatus.INVALID_APPOINTMENT_RULES;
        }

        String normalizedCustomerName = customerName.trim();
        String normalizedSlotTime = slotTime.trim();
        boolean matchingSlotFound = false;

        for (AppointmentSlot slot : appointmentRepository.findAll()) {
            if (slot.getTime().equals(normalizedSlotTime)) {
                matchingSlotFound = true;

                if (!slot.isAvailable()) {
                    continue;
                }

                slot.book();
                Appointment appointment = new Appointment(
                        normalizedCustomerName,
                        normalizedSlotTime,
                        durationMinutes,
                        participantCount,
                        AppointmentStatus.CONFIRMED,
                        normalizedType
                );
                appointmentBookingRepository.save(appointment);
                sendPendingNotificationIfConfigured(appointment);
                return BookingStatus.SUCCESS;
            }
        }

        if (matchingSlotFound) {
            return BookingStatus.SLOT_ALREADY_BOOKED;
        }

        return BookingStatus.SLOT_NOT_FOUND;
    }

    /**
     * Returns the managed reservations.
     *
     * @return collection with the requested results
     */
    public List<Appointment> getManagedReservations() {
        if (!isCurrentUserAdmin()) {
            return Collections.emptyList();
        }
        return appointmentBookingRepository.findAll();
    }

    /**
     * Checks whether it can current user manage reservations.
     *
     * @return true when the action is valid or successful, otherwise false
     */
    public boolean canCurrentUserManageReservations() {
        return isCurrentUserAdmin();
    }

    /**
     * Returns the reservations for customer.
     *
     * @param customerEmail email address used for login or matching
     * @return collection with the requested results
     */
    public List<Appointment> getReservationsForCustomer(String customerEmail) {
        String normalizedCustomerEmail = normalize(customerEmail);
        if (normalizedCustomerEmail == null || sessionManager == null || !sessionManager.isLoggedIn()) {
            return Collections.emptyList();
        }

        String currentEmail = normalize(sessionManager.getCurrentEmail());
        boolean isAdmin = sessionManager.isAdmin();

        if (!isAdmin && !normalizedCustomerEmail.equalsIgnoreCase(currentEmail)) {
            return Collections.emptyList();
        }

        List<Appointment> results = new ArrayList<>();
        for (Appointment appointment : appointmentBookingRepository.findAll()) {
            String bookingCustomer = normalize(appointment.getCustomerName());
            if (normalizedCustomerEmail.equalsIgnoreCase(bookingCustomer)) {
                results.add(appointment);
            }
        }
        return results;
    }

    /**
     * Checks whether it can cancel appointment.
     *
     * @param appointmentId unique id used to find the record
     * @return status that explains the operation result
     */
    public BookingStatus cancelAppointment(String appointmentId) {
        return cancelReservationInternal(appointmentId, ReservationAccess.ADMIN_ANY);
    }

    /**
     * Approves appointment when allowed.
     *
     * @param appointmentId unique id used to find the record
     * @return status that explains the operation result
     */
    public BookingStatus approveAppointment(String appointmentId) {
        Appointment appointment = resolveAuthorizedAppointment(appointmentId, ReservationAccess.ADMIN_ANY);
        if (appointment == null) {
            return resolveAuthorizationOrNotFoundStatus(appointmentId, ReservationAccess.ADMIN_ANY);
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return BookingStatus.APPOINTMENT_ALREADY_CANCELLED;
        }
        if (!appointment.isFutureComparedTo(LocalDateTime.now())) {
            return BookingStatus.APPOINTMENT_NOT_FUTURE;
        }

        Appointment updated = appointment.withStatus(AppointmentStatus.CONFIRMED);
        if (!appointmentBookingRepository.update(updated)) {
            return BookingStatus.UPDATE_FAILED;
        }

        notifyEvent("Reservation approved: " + updated.getId());
        sendApprovedNotificationIfConfigured(updated);
        return BookingStatus.SUCCESS;
    }

    /**
     * Checks whether it can cancel own appointment.
     *
     * @param appointmentId unique id used to find the record
     * @return status that explains the operation result
     */
    public BookingStatus cancelOwnAppointment(String appointmentId) {
        return cancelReservationInternal(appointmentId, ReservationAccess.OWN_ONLY);
    }

    /**
     * Changes appointment using new input.
     *
     * @param appointmentId unique id used to find the record
     * @param newSlotTime slot time text like 10:00
     * @return status that explains the operation result
     */
    public BookingStatus modifyAppointment(String appointmentId, String newSlotTime) {
        return modifyReservationInternal(appointmentId, newSlotTime, ReservationAccess.ADMIN_ANY);
    }

    /**
     * Changes own appointment using new input.
     *
     * @param appointmentId unique id used to find the record
     * @param newSlotTime slot time text like 10:00
     * @return status that explains the operation result
     */
    public BookingStatus modifyOwnAppointment(String appointmentId, String newSlotTime) {
        return modifyReservationInternal(appointmentId, newSlotTime, ReservationAccess.OWN_ONLY);
    }

    /**
     * Checks whether it can cancel reservation internal.
     *
     * @param appointmentId unique id used to find the record
     * @param access value for access
     * @return status that explains the operation result
     */
    private BookingStatus cancelReservationInternal(String appointmentId, ReservationAccess access) {
        Appointment appointment = resolveAuthorizedAppointment(appointmentId, access);
        if (appointment == null) {
            return resolveAuthorizationOrNotFoundStatus(appointmentId, access);
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
        sendCancelledNotificationIfConfigured(updated);
        return BookingStatus.SUCCESS;
    }

    /**
     * Changes reservation internal using new input.
     *
     * @param appointmentId unique id used to find the record
     * @param newSlotTime slot time text like 10:00
     * @param access value for access
     * @return status that explains the operation result
     */
    private BookingStatus modifyReservationInternal(
            String appointmentId,
            String newSlotTime,
            ReservationAccess access
    ) {
        if (newSlotTime == null || newSlotTime.trim().isEmpty()) {
            return BookingStatus.BLANK_SLOT_TIME;
        }

        Appointment appointment = resolveAuthorizedAppointment(appointmentId, access);
        if (appointment == null) {
            return resolveAuthorizationOrNotFoundStatus(appointmentId, access);
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
        Appointment updated = appointment.withSlotTimeAndStatus(
                normalizedNewSlotTime,
                AppointmentStatus.MODIFIED
        );

        if (!appointmentBookingRepository.update(updated)) {
            targetSlot.release();
            if (currentSlot != null) {
                currentSlot.book();
            }
            return BookingStatus.UPDATE_FAILED;
        }

        notifyEvent("Reservation modified: " + updated.getId() + " -> " + normalizedNewSlotTime);
        sendModifiedNotificationIfConfigured(updated);
        return BookingStatus.SUCCESS;
    }

    /**
     * Runs resolve authorized appointment for this class.
     *
     * @param appointmentId unique id used to find the record
     * @param access value for access
     * @return result produced by this method
     */
    private Appointment resolveAuthorizedAppointment(String appointmentId, ReservationAccess access) {
        if (!isSessionActive()) {
            return null;
        }
        if (access == ReservationAccess.ADMIN_ANY && !isCurrentUserAdmin()) {
            return null;
        }

        Appointment appointment = appointmentBookingRepository.findById(normalize(appointmentId)).orElse(null);
        if (appointment == null) {
            return null;
        }

        if (access == ReservationAccess.OWN_ONLY && !isOwnedByCurrentUser(appointment)) {
            return null;
        }

        return appointment;
    }

    /**
     * Runs resolve authorization or not found status for this class.
     *
     * @param appointmentId unique id used to find the record
     * @param access value for access
     * @return status that explains the operation result
     */
    private BookingStatus resolveAuthorizationOrNotFoundStatus(String appointmentId, ReservationAccess access) {
        if (!isSessionActive()) {
            return BookingStatus.UNAUTHORIZED;
        }

        if (access == ReservationAccess.ADMIN_ANY && !isCurrentUserAdmin()) {
            return BookingStatus.UNAUTHORIZED;
        }

        Appointment appointment = appointmentBookingRepository.findById(normalize(appointmentId)).orElse(null);
        if (appointment == null) {
            return BookingStatus.APPOINTMENT_NOT_FOUND;
        }

        if (access == ReservationAccess.OWN_ONLY && !isOwnedByCurrentUser(appointment)) {
            return BookingStatus.UNAUTHORIZED;
        }

        return BookingStatus.APPOINTMENT_NOT_FOUND;
    }

    /**
     * Finds slot by time using the given input.
     *
     * @param slotTime slot time text like 10:00
     * @return result produced by this method
     */
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
     * Checks whether current user admin is true.
     *
     * @return true when the action is valid or successful, otherwise false
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

    /**
     * Checks whether session active is true.
     *
     * @return true when the action is valid or successful, otherwise false
     */
    private boolean isSessionActive() {
        return sessionManager != null && sessionManager.isLoggedIn();
    }

    /**
     * Checks whether owned by current user is true.
     *
     * @param appointment value for appointment
     * @return true when the action is valid or successful, otherwise false
     */
    private boolean isOwnedByCurrentUser(Appointment appointment) {
        if (appointment == null || !isSessionActive()) {
            return false;
        }

        String currentEmail = normalize(sessionManager.getCurrentEmail());
        String ownerIdentity = normalize(appointment.getCustomerName());
        return ownerIdentity != null
                && ownerIdentity.equalsIgnoreCase(currentEmail);
    }

    /**
     * Sends event to listeners.
     *
     * @param message message text to show or send
     */
    private void notifyEvent(String message) {
        if (eventManager != null && message != null && !message.trim().isEmpty()) {
            eventManager.notifyObservers(message);
        }
    }

    /**
     * Sends a pending notification when the coordinator is configured.
     *
     * @param appointment appointment involved in this action
     */
    private void sendPendingNotificationIfConfigured(Appointment appointment) {
        if (appointmentNotificationCoordinator != null && appointment != null) {
            appointmentNotificationCoordinator.sendPendingNotification(appointment);
        }
    }

    /**
     * Sends an approved notification when the coordinator is configured.
     *
     * @param appointment appointment involved in this action
     */
    private void sendApprovedNotificationIfConfigured(Appointment appointment) {
        if (appointmentNotificationCoordinator != null && appointment != null) {
            appointmentNotificationCoordinator.sendApprovedNotification(appointment);
        }
    }

    /**
     * Sends a cancelled notification when the coordinator is configured.
     *
     * @param appointment appointment involved in this action
     */
    private void sendCancelledNotificationIfConfigured(Appointment appointment) {
        if (appointmentNotificationCoordinator != null && appointment != null) {
            appointmentNotificationCoordinator.sendCancelledNotification(appointment);
        }
    }

    /**
     * Sends a modified notification when the coordinator is configured.
     *
     * @param appointment appointment involved in this action
     */
    private void sendModifiedNotificationIfConfigured(Appointment appointment) {
        if (appointmentNotificationCoordinator != null && appointment != null) {
            appointmentNotificationCoordinator.sendModifiedNotification(appointment);
        }
    }

    /**
     * Runs normalize for this class.
     *
     * @param value value used by this method
     * @return text result from this method
     */
    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    /**
     * Converts text into integer.
     *
     * @param value value used by this method
     * @return result produced by this method
     */
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

    /**
     * Runs validation probe for this class.
     *
     * @param durationMinutes appointment duration in minutes
     * @param participantCount number of people for the appointment
     * @return result produced by this method
     */
    private Appointment validationProbe(int durationMinutes, int participantCount) {
        return new Appointment("validation", LocalDateTime.now(), durationMinutes, participantCount);
    }

    /**
     * Runs type rules allow for this class.
     *
     * @param appointment value for appointment
     * @param appointmentType value for appointment type
     * @return true when the action is valid or successful, otherwise false
     */
    private boolean typeRulesAllow(Appointment appointment, AppointmentType appointmentType) {
        AppointmentType normalizedType = normalizeType(appointmentType);
        AppointmentTypeRule rule = appointmentTypeRules.get(normalizedType);

        if (rule == null) {
            return false;
        }

        return rule.isValid(appointment);
    }

    /**
     * Runs normalize type for this class.
     *
     * @param appointmentType value for appointment type
     * @return result produced by this method
     */
    private AppointmentType normalizeType(AppointmentType appointmentType) {
        return appointmentType == null ? AppointmentType.NORMAL : appointmentType;
    }

    /**
     * Builds type rule map from current data.
     *
     * @param rules value for rules
     * @return result produced by this method
     */
    private Map<AppointmentType, AppointmentTypeRule> buildTypeRuleMap(List<AppointmentTypeRule> rules) {
        Map<AppointmentType, AppointmentTypeRule> strategyMap = new EnumMap<>(AppointmentType.class);
        for (AppointmentTypeRule rule : rules) {
            strategyMap.put(rule.getSupportedType(), rule);
        }

        if (!strategyMap.keySet().containsAll(EnumSet.allOf(AppointmentType.class))) {
            throw new IllegalStateException("A validation rule must exist for each appointment type.");
        }

        return strategyMap;
    }

    private enum ReservationAccess {
        ADMIN_ANY,
        OWN_ONLY
    }
}