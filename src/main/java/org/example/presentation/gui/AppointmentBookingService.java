package org.example.presentation.gui;

import org.example.domain.Appointment;
import org.example.domain.AppointmentSlot;
import org.example.domain.AppointmentStatus;
import org.example.domain.AppointmentType;
import org.example.domain.UserRole;
import org.example.repository.AppointmentBookingRepository;
import org.example.repository.AppointmentRepository;
import org.example.repository.UserRepository;
import org.example.service.*;

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
 *
 * @author appointment-system
 * @version 1.0
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
     * @param userRepository user repository used for authorization checks
     * @param eventManager manager object used for observer notifications
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

        return bookAppointment(
                customerName,
                slotTime,
                durationMinutes,
                participantCount,
                appointmentType
        );
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
        return bookAppointment(
                customerName,
                slotTime,
                durationMinutes,
                participantCount,
                AppointmentType.NORMAL
        );
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

        Appointment normalizedProbe = validationProbe(durationMinutes, participantCount);
        AppointmentType normalizedType = normalizeType(appointmentType);
        normalizedProbe.setType(normalizedType);

        if (!durationRule.isValid(normalizedProbe)) {
            return BookingStatus.INVALID_DURATION;
        }

        if (!participantRule.isValid(normalizedProbe)) {
            return BookingStatus.INVALID_PARTICIPANT_COUNT;
        }

        if (!typeRulesAllow(normalizedProbe, normalizedType)) {
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

                appointmentBookingRepository.save(new Appointment(
                        normalizedCustomerName,
                        normalizedSlotTime,
                        durationMinutes,
                        participantCount,
                        AppointmentStatus.CONFIRMED,
                        normalizedType
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
     * Checks whether current user can manage reservations.
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
     * Cancels an appointment as admin.
     *
     * @param appointmentId unique id used to find the record
     * @return status that explains the operation result
     */
    public BookingStatus cancelAppointment(String appointmentId) {
        return cancelReservationInternal(appointmentId, ReservationAccess.ADMIN_ANY);
    }

    /**
     * Cancels the current user's own appointment.
     *
     * @param appointmentId unique id used to find the record
     * @return status that explains the operation result
     */
    public BookingStatus cancelOwnAppointment(String appointmentId) {
        return cancelReservationInternal(appointmentId, ReservationAccess.OWN_ONLY);
    }

    /**
     * Modifies an appointment as admin.
     *
     * @param appointmentId unique id used to find the record
     * @param newSlotTime slot time text like 10:00
     * @return status that explains the operation result
     */
    public BookingStatus modifyAppointment(String appointmentId, String newSlotTime) {
        return modifyReservationInternal(appointmentId, newSlotTime, ReservationAccess.ADMIN_ANY);
    }

    /**
     * Modifies the current user's own appointment.
     *
     * @param appointmentId unique id used to find the record
     * @param newSlotTime slot time text like 10:00
     * @return status that explains the operation result
     */
    public BookingStatus modifyOwnAppointment(String appointmentId, String newSlotTime) {
        return modifyReservationInternal(appointmentId, newSlotTime, ReservationAccess.OWN_ONLY);
    }

    /**
     * Cancels a reservation using the given access level.
     *
     * @param appointmentId unique id used to find the record
     * @param access access mode used by the caller
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
        return BookingStatus.SUCCESS;
    }

    /**
     * Modifies a reservation using the given access level.
     *
     * @param appointmentId unique id used to find the record
     * @param newSlotTime slot time text like 10:00
     * @param access access mode used by the caller
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
        return BookingStatus.SUCCESS;
    }

    /**
     * Resolves an appointment only if current caller is authorized.
     *
     * @param appointmentId unique id used to find the record
     * @param access access mode used by the caller
     * @return authorized appointment or null
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
     * Resolves the appropriate status when authorization or lookup fails.
     *
     * @param appointmentId unique id used to find the record
     * @param access access mode used by the caller
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
     * @return slot if found, otherwise null
     */
    private AppointmentSlot findSlotByTime(String slotTime) {
        String normalizedSlotTime = normalize(slotTime);
        if (normalizedSlotTime == null) {
            return null;
        }

        for (AppointmentSlot slot : appointmentRepository.findAll()) {
            if (normalizedSlotTime.equals(slot.getTime())) {
                return slot;
            }
        }
        return null;
    }

    /**
     * Checks whether current logged-in user is an administrator.
     *
     * @return true when the current user is admin, otherwise false
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
     * Checks whether a session is currently active.
     *
     * @return true when logged in, otherwise false
     */
    private boolean isSessionActive() {
        return sessionManager != null && sessionManager.isLoggedIn();
    }

    /**
     * Checks whether the given appointment belongs to the current user.
     *
     * @param appointment appointment to validate ownership for
     * @return true when the current user owns the appointment, otherwise false
     */
    private boolean isOwnedByCurrentUser(Appointment appointment) {
        if (appointment == null || !isSessionActive()) {
            return false;
        }

        String currentEmail = normalize(sessionManager.getCurrentEmail());
        String ownerIdentity = normalize(appointment.getCustomerName());
        return ownerIdentity != null && ownerIdentity.equalsIgnoreCase(currentEmail);
    }

    /**
     * Sends event notification to registered observers.
     *
     * @param message message text to show or send
     */
    private void notifyEvent(String message) {
        if (eventManager != null && message != null && !message.trim().isEmpty()) {
            eventManager.notifyObservers(message);
        }
    }

    /**
     * Normalizes text input by trimming it.
     *
     * @param value raw input
     * @return trimmed value, or null if blank
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
     * @param value raw input
     * @return parsed integer, or null when invalid
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
     * Creates a temporary appointment used only for validation.
     *
     * @param durationMinutes appointment duration in minutes
     * @param participantCount number of people for the appointment
     * @return temporary appointment instance
     */
    private Appointment validationProbe(int durationMinutes, int participantCount) {
        return new Appointment("validation", LocalDateTime.now(), durationMinutes, participantCount);
    }

    /**
     * Applies type-specific rules for an appointment.
     *
     * @param appointment appointment to validate
     * @param appointmentType type to validate against
     * @return true when the type rule accepts the appointment, otherwise false
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
     * Normalizes null appointment types to NORMAL.
     *
     * @param appointmentType appointment type
     * @return normalized appointment type
     */
    private AppointmentType normalizeType(AppointmentType appointmentType) {
        return appointmentType == null ? AppointmentType.NORMAL : appointmentType;
    }

    /**
     * Builds a rule map for all supported appointment types.
     *
     * @param rules list of type rules
     * @return immutable-ready enum map with one rule per type
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

    /**
     * Represents reservation access scope.
     */
    private enum ReservationAccess {
        ADMIN_ANY,
        OWN_ONLY
    }
}