package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentSlot;
import org.example.domain.AppointmentStatus;
import org.example.domain.AppointmentType;
import org.example.domain.WaitlistEntry;
import org.example.domain.UserRole;
import org.example.repository.AppointmentBookingRepository;
import org.example.repository.AppointmentRepository;
import org.example.repository.WaitlistRepository;
import org.example.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents appointment booking service in the system.
 */
public class AppointmentBookingService {

    private static final String LEGACY_PHONE_PLACEHOLDER = "N/A";
    private static final String PHONE_PATTERN = "^\\+?[0-9][0-9\\s-]{6,14}$";

    private final AppointmentRepository appointmentRepository;
    private final AppointmentBookingRepository appointmentBookingRepository;
    private final BookingRuleStrategy durationRule;
    private final BookingRuleStrategy participantRule;
    private final Map<AppointmentType, AppointmentTypeRule> appointmentTypeRules;
    private final SessionManager sessionManager;
    private final UserRepository userRepository;
    private final EventManager eventManager;
    private final WaitlistRepository waitlistRepository;
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
        this(appointmentRepository, appointmentBookingRepository, null, null, null, null, null);
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
        this(appointmentRepository, appointmentBookingRepository, sessionManager, userRepository, eventManager, null, null);
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
        this(appointmentRepository, appointmentBookingRepository, sessionManager, userRepository, eventManager, null, appointmentNotificationCoordinator);
    }

    /**
     * Creates a new appointment booking service object with the given values.
     *
     * @param appointmentRepository repository used to read and save data
     * @param appointmentBookingRepository repository used to read and save data
     * @param sessionManager manager object used for shared app state
     * @param userRepository user involved in this action
     * @param eventManager manager object used for shared app state
     * @param waitlistRepository repository used to store waitlisted entries
     * @param appointmentNotificationCoordinator coordinator used to send appointment notifications
     */
    public AppointmentBookingService(
            AppointmentRepository appointmentRepository,
            AppointmentBookingRepository appointmentBookingRepository,
            SessionManager sessionManager,
            UserRepository userRepository,
            EventManager eventManager,
            WaitlistRepository waitlistRepository,
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
        this.waitlistRepository = waitlistRepository;
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
        return bookAppointmentInternal(
                customerName,
                LEGACY_PHONE_PLACEHOLDER,
                slotTime,
                durationMinutesInput,
                participantCountInput,
                AppointmentType.NORMAL,
                false
        );
    }

    /**
     * Books appointment when allowed.
     *
     * @param customerName value for customer name
     * @param customerPhoneNumber value for customer phone number
     * @param slotTime slot time text like 10:00
     * @param durationMinutesInput appointment duration in minutes
     * @param participantCountInput number of people for the appointment
     * @return status that explains the operation result
     */
    public BookingStatus bookAppointment(
            String customerName,
            String customerPhoneNumber,
            String slotTime,
            String durationMinutesInput,
            String participantCountInput
    ) {
        return bookAppointmentInternal(
                customerName,
                customerPhoneNumber,
                slotTime,
                durationMinutesInput,
                participantCountInput,
                AppointmentType.NORMAL,
                true
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
        return bookAppointmentInternal(
                customerName,
                LEGACY_PHONE_PLACEHOLDER,
                slotTime,
                durationMinutesInput,
                participantCountInput,
                appointmentType,
                false
        );
    }

    /**
     * Books appointment when allowed.
     *
     * @param customerName value for customer name
     * @param customerPhoneNumber value for customer phone number
     * @param slotTime slot time text like 10:00
     * @param durationMinutesInput appointment duration in minutes
     * @param participantCountInput number of people for the appointment
     * @param appointmentType value for appointment type
     * @return status that explains the operation result
     */
    public BookingStatus bookAppointment(
            String customerName,
            String customerPhoneNumber,
            String slotTime,
            String durationMinutesInput,
            String participantCountInput,
            AppointmentType appointmentType
    ) {
        return bookAppointmentInternal(
                customerName,
                customerPhoneNumber,
                slotTime,
                durationMinutesInput,
                participantCountInput,
                appointmentType,
                true
        );
    }

    /**
     * Books appointment when allowed.
     *
     * @param customerName value for customer name
     * @param customerPhoneNumber value for customer phone number
     * @param slotTime slot time text like 10:00
     * @param durationMinutesInput appointment duration in minutes
     * @param participantCountInput number of people for the appointment
     * @param appointmentType value for appointment type
     * @param strictPhoneValidation flag that enables strict phone validation
     * @return status that explains the operation result
     */
    private BookingStatus bookAppointmentInternal(
            String customerName,
            String customerPhoneNumber,
            String slotTime,
            String durationMinutesInput,
            String participantCountInput,
            AppointmentType appointmentType,
            boolean strictPhoneValidation
    ) {
        if (customerName == null || customerName.trim().isEmpty()) {
            return BookingStatus.BLANK_CUSTOMER_NAME;
        }
        if (strictPhoneValidation && (customerPhoneNumber == null || customerPhoneNumber.trim().isEmpty())) {
            return BookingStatus.BLANK_PHONE_NUMBER;
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

        if (strictPhoneValidation && !isValidPhoneNumber(customerPhoneNumber)) {
            return BookingStatus.INVALID_PHONE_NUMBER;
        }

        return bookAppointmentInternal(
                customerName,
                customerPhoneNumber,
                slotTime,
                durationMinutes,
                participantCount,
                appointmentType,
                strictPhoneValidation
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
        return bookAppointmentInternal(
                customerName,
                LEGACY_PHONE_PLACEHOLDER,
                slotTime,
                durationMinutes,
                participantCount,
                AppointmentType.NORMAL,
                false
        );
    }

    /**
     * Books appointment when allowed.
     *
     * @param customerName value for customer name
     * @param customerPhoneNumber value for customer phone number
     * @param slotTime slot time text like 10:00
     * @param durationMinutes appointment duration in minutes
     * @param participantCount number of people for the appointment
     * @return status that explains the operation result
     */
    public BookingStatus bookAppointment(
            String customerName,
            String customerPhoneNumber,
            String slotTime,
            int durationMinutes,
            int participantCount
    ) {
        return bookAppointmentInternal(
                customerName,
                customerPhoneNumber,
                slotTime,
                durationMinutes,
                participantCount,
                AppointmentType.NORMAL,
                true
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
        return bookAppointmentInternal(
                customerName,
                LEGACY_PHONE_PLACEHOLDER,
                slotTime,
                durationMinutes,
                participantCount,
                appointmentType,
                false
        );
    }

    /**
     * Books appointment when allowed.
     *
     * @param customerName value for customer name
     * @param customerPhoneNumber value for customer phone number
     * @param slotTime slot time text like 10:00
     * @param durationMinutes appointment duration in minutes
     * @param participantCount number of people for the appointment
     * @param appointmentType value for appointment type
     * @return status that explains the operation result
     */
    public BookingStatus bookAppointment(
            String customerName,
            String customerPhoneNumber,
            String slotTime,
            int durationMinutes,
            int participantCount,
            AppointmentType appointmentType
    ) {
        return bookAppointmentInternal(
                customerName,
                customerPhoneNumber,
                slotTime,
                durationMinutes,
                participantCount,
                appointmentType,
                true
        );
    }

    /**
     * Books appointment when allowed.
     *
     * @param customerName value for customer name
     * @param customerPhoneNumber value for customer phone number
     * @param slotTime slot time text like 10:00
     * @param durationMinutes appointment duration in minutes
     * @param participantCount number of people for the appointment
     * @param appointmentType value for appointment type
     * @param strictPhoneValidation flag that enables strict phone validation
     * @return status that explains the operation result
     */
    private BookingStatus bookAppointmentInternal(
            String customerName,
            String customerPhoneNumber,
            String slotTime,
            int durationMinutes,
            int participantCount,
            AppointmentType appointmentType,
            boolean strictPhoneValidation
    ) {
        if (customerName == null || customerName.trim().isEmpty()) {
            return BookingStatus.BLANK_CUSTOMER_NAME;
        }
        if (strictPhoneValidation && (customerPhoneNumber == null || customerPhoneNumber.trim().isEmpty())) {
            return BookingStatus.BLANK_PHONE_NUMBER;
        }
        if (slotTime == null || slotTime.trim().isEmpty()) {
            return BookingStatus.BLANK_SLOT_TIME;
        }
        if (strictPhoneValidation && !isValidPhoneNumber(customerPhoneNumber)) {
            return BookingStatus.INVALID_PHONE_NUMBER;
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
        String normalizedSlotSelection = slotTime.trim();
        boolean matchingSlotFound = false;
        AppointmentSlot matchingUnavailableSlot = null;

        String bookingCustomerName = normalizedCustomerName;
        String bookingCustomerEmail = normalizedCustomerName;
        String normalizedPhone = normalize(customerPhoneNumber);
        if (sessionManager != null && sessionManager.isLoggedIn() && sessionManager.getCurrentUser() != null) {
            bookingCustomerEmail = normalize(sessionManager.getCurrentUser().getEmail());
            if (bookingCustomerEmail == null) {
                bookingCustomerEmail = normalizedCustomerName;
            }
        }

        for (AppointmentSlot slot : appointmentRepository.findAll()) {
            if (slot.matchesSelection(normalizedSlotSelection)) {
                matchingSlotFound = true;

                if (!slot.isAvailable()) {
                    if (matchingUnavailableSlot == null) {
                        matchingUnavailableSlot = slot;
                    }
                    continue;
                }

                slot.book();
                Appointment appointment = new Appointment(
                        UUID.randomUUID().toString(),
                        bookingCustomerName,
                        bookingCustomerEmail,
                        slot.getDateTime(),
                        durationMinutes,
                        participantCount,
                        AppointmentStatus.CONFIRMED
                ).withCustomerPhoneNumber(normalizedPhone);
                appointment.setType(normalizedType);
                appointmentBookingRepository.save(appointment);
                sendPendingNotificationIfConfigured(appointment);
                return BookingStatus.SUCCESS;
            }
        }

        if (matchingSlotFound) {
            if (waitlistRepository != null && matchingUnavailableSlot != null) {
                return addToWaitlistForFullSlot(
                        matchingUnavailableSlot,
                        bookingCustomerName,
                        bookingCustomerEmail,
                        normalizedPhone,
                        durationMinutes,
                        participantCount,
                        normalizedType
                );
            }
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

        String resolvedCustomerId = null;
        if (userRepository != null) {
            resolvedCustomerId = userRepository.findByEmail(normalizedCustomerEmail)
                    .map(user -> normalize(user.getId()))
                    .orElse(null);
        }

        List<Appointment> results = new ArrayList<>();
        for (Appointment appointment : appointmentBookingRepository.findAll()) {
            String bookingCustomerName = normalize(appointment.getCustomerName());
            String bookingCustomerEmail = appointment.getUser() == null ? null : normalize(appointment.getUser().getEmail());
            if (normalizedCustomerEmail.equalsIgnoreCase(bookingCustomerEmail)
                    || normalizedCustomerEmail.equalsIgnoreCase(bookingCustomerName)
                    || (resolvedCustomerId != null && resolvedCustomerId.equalsIgnoreCase(bookingCustomerName))
                    || (resolvedCustomerId != null && resolvedCustomerId.equalsIgnoreCase(bookingCustomerEmail))) {
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
     * Marks appointment as attended when allowed.
     *
     * @param appointmentId unique id used to find the record
     * @return status that explains the operation result
     */
    public BookingStatus markAppointmentAsAttended(String appointmentId) {
        Appointment appointment = resolveAuthorizedAppointment(appointmentId, ReservationAccess.ADMIN_ANY);
        if (appointment == null) {
            return resolveAuthorizationOrNotFoundStatus(appointmentId, ReservationAccess.ADMIN_ANY);
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return BookingStatus.APPOINTMENT_ALREADY_CANCELLED;
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            return BookingStatus.APPOINTMENT_ALREADY_COMPLETED;
        }
        if (appointment.getStatus() == AppointmentStatus.ATTENDED) {
            return BookingStatus.APPOINTMENT_ALREADY_ATTENDED;
        }
        if (appointment.getStatus() == AppointmentStatus.NOT_ATTENDED) {
            return BookingStatus.APPOINTMENT_ALREADY_NOT_ATTENDED;
        }

        Appointment updated = appointment.withStatus(AppointmentStatus.ATTENDED);
        if (!appointmentBookingRepository.update(updated)) {
            return BookingStatus.UPDATE_FAILED;
        }

        notifyEvent("Reservation attended: " + updated.getId());
        sendAttendedNotificationIfConfigured(updated);
        return BookingStatus.SUCCESS;
    }

    /**
     * Marks appointment as completed when allowed.
     *
     * @param appointmentId unique id used to find the record
     * @return status that explains the operation result
     */
    public BookingStatus markAppointmentAsCompleted(String appointmentId) {
        Appointment appointment = resolveAuthorizedAppointment(appointmentId, ReservationAccess.ADMIN_ANY);
        if (appointment == null) {
            return resolveAuthorizationOrNotFoundStatus(appointmentId, ReservationAccess.ADMIN_ANY);
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return BookingStatus.APPOINTMENT_ALREADY_CANCELLED;
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            return BookingStatus.APPOINTMENT_ALREADY_COMPLETED;
        }
        if (appointment.getStatus() == AppointmentStatus.NOT_ATTENDED) {
            return BookingStatus.APPOINTMENT_ALREADY_NOT_ATTENDED;
        }
        if (appointment.getStatus() != AppointmentStatus.ATTENDED) {
            return BookingStatus.APPOINTMENT_NOT_ATTENDED;
        }

        Appointment updated = appointment.withStatus(AppointmentStatus.COMPLETED);
        if (!appointmentBookingRepository.update(updated)) {
            return BookingStatus.UPDATE_FAILED;
        }

        notifyEvent("Reservation completed: " + updated.getId());
        sendCompletedNotificationIfConfigured(updated);
        return BookingStatus.SUCCESS;
    }

    /**
     * Marks appointment as not attended when allowed.
     *
     * @param appointmentId unique id used to find the record
     * @return status that explains the operation result
     */
    public BookingStatus markAppointmentAsNotAttended(String appointmentId) {
        Appointment appointment = resolveAuthorizedAppointment(appointmentId, ReservationAccess.ADMIN_ANY);
        if (appointment == null) {
            return resolveAuthorizationOrNotFoundStatus(appointmentId, ReservationAccess.ADMIN_ANY);
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return BookingStatus.APPOINTMENT_ALREADY_CANCELLED;
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            return BookingStatus.APPOINTMENT_ALREADY_COMPLETED;
        }
        if (appointment.getStatus() == AppointmentStatus.ATTENDED) {
            return BookingStatus.APPOINTMENT_ALREADY_ATTENDED;
        }
        if (appointment.getStatus() == AppointmentStatus.NOT_ATTENDED) {
            return BookingStatus.APPOINTMENT_ALREADY_NOT_ATTENDED;
        }

        Appointment updated = appointment.withStatus(AppointmentStatus.NOT_ATTENDED);
        if (!appointmentBookingRepository.update(updated)) {
            return BookingStatus.UPDATE_FAILED;
        }

        notifyEvent("Reservation marked as not attended: " + updated.getId());
        sendNotAttendedNotificationIfConfigured(updated);
        return BookingStatus.SUCCESS;
    }

    /**
     * Submits feedback for the current user's own completed appointment.
     *
     * @param appointmentId unique id used to find the record
     * @param rating rating value from 1 to 5
     * @param feedbackComment comment text entered by the user
     * @return status that explains the operation result
     */
    public BookingStatus submitFeedback(String appointmentId, int rating, String feedbackComment) {
        return submitFeedbackInternal(appointmentId, rating, feedbackComment, ReservationAccess.OWN_ONLY);
    }

    /**
     * Submits feedback for the current user's own completed appointment.
     *
     * @param appointmentId unique id used to find the record
     * @param rating rating value from 1 to 5
     * @param feedbackComment comment text entered by the user
     * @return status that explains the operation result
     */
    public BookingStatus submitOwnFeedback(String appointmentId, int rating, String feedbackComment) {
        return submitFeedbackInternal(appointmentId, rating, feedbackComment, ReservationAccess.OWN_ONLY);
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
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            return BookingStatus.APPOINTMENT_ALREADY_COMPLETED;
        }
        if (!appointment.isFutureComparedTo(LocalDateTime.now())) {
            return BookingStatus.APPOINTMENT_NOT_FUTURE;
        }

        AppointmentSlot slot = findSlotForAppointment(appointment);
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

        BookingStatus waitlistPromotionStatus = promoteWaitlistedAppointmentIfAvailable(slot);
        return waitlistPromotionStatus == BookingStatus.SUCCESS ? BookingStatus.SUCCESS : waitlistPromotionStatus;
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
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            return BookingStatus.APPOINTMENT_ALREADY_COMPLETED;
        }
        if (appointment.getStatus() == AppointmentStatus.ATTENDED) {
            return BookingStatus.APPOINTMENT_ALREADY_ATTENDED;
        }
        if (appointment.getStatus() == AppointmentStatus.NOT_ATTENDED) {
            return BookingStatus.APPOINTMENT_ALREADY_NOT_ATTENDED;
        }
        if (!appointment.isFutureComparedTo(LocalDateTime.now())) {
            return BookingStatus.APPOINTMENT_NOT_FUTURE;
        }

        String normalizedNewSlotSelection = newSlotTime.trim();
        AppointmentSlot targetSlot = findSlotBySelection(normalizedNewSlotSelection);
        if (targetSlot == null) {
            return BookingStatus.SLOT_NOT_FOUND;
        }
        if (!targetSlot.isFutureSlot()) {
            return BookingStatus.APPOINTMENT_NOT_FUTURE;
        }
        if (!targetSlot.isAvailable()) {
            return BookingStatus.SLOT_ALREADY_BOOKED;
        }

        AppointmentSlot currentSlot = findSlotForAppointment(appointment);
        if (currentSlot != null) {
            currentSlot.release();
        }

        targetSlot.book();
        Appointment updated = appointment.withStartTimeAndStatus(
                targetSlot.getDateTime(),
                AppointmentStatus.RESCHEDULED
        );

        if (!appointmentBookingRepository.update(updated)) {
            targetSlot.release();
            if (currentSlot != null) {
                currentSlot.book();
            }
            return BookingStatus.UPDATE_FAILED;
        }

        notifyEvent("Reservation modified: " + updated.getId() + " -> " + updated.getSlotDateTimeLabel());
        sendRescheduledNotificationIfConfigured(appointment, updated);
        return BookingStatus.SUCCESS;
    }

    /**
     * Saves feedback for a completed appointment.
     *
     * @param appointmentId unique id used to find the record
     * @param rating rating value from 1 to 5
     * @param feedbackComment comment text entered by the user
     * @param access value for access
     * @return status that explains the operation result
     */
    private BookingStatus submitFeedbackInternal(
            String appointmentId,
            int rating,
            String feedbackComment,
            ReservationAccess access
    ) {
        if (rating < 1 || rating > 5) {
            return BookingStatus.INVALID_RATING;
        }

        Appointment appointment = resolveAuthorizedAppointment(appointmentId, access);
        if (appointment == null) {
            return resolveAuthorizationOrNotFoundStatus(appointmentId, access);
        }

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            return BookingStatus.APPOINTMENT_NOT_COMPLETED;
        }

        if (appointment.isFeedbackSubmitted()) {
            return BookingStatus.FEEDBACK_ALREADY_SUBMITTED;
        }

        Appointment updated = appointment.withStatus(AppointmentStatus.COMPLETED);
        updated.setRating(rating);
        updated.setFeedbackComment(normalizeFeedbackComment(feedbackComment));
        updated.setFeedbackSubmitted(true);

        if (!appointmentBookingRepository.update(updated)) {
            return BookingStatus.UPDATE_FAILED;
        }

        notifyEvent("Feedback submitted: " + updated.getId());
        return BookingStatus.SUCCESS;
    }

    /**
     * Returns the appointment only when access rules allow it.
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
     * Resolves whether access is unauthorized or appointment is missing.
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
     * @param slotSelection slot text like yyyy-MM-dd (DAY) HH:mm
     * @return matching slot, or null when no slot matches
     */
    private AppointmentSlot findSlotBySelection(String slotSelection) {
        String normalized = normalize(slotSelection);
        if (normalized == null) {
            return null;
        }

        for (AppointmentSlot slot : appointmentRepository.findAll()) {
            if (slot.matchesSelection(normalized)) {
                return slot;
            }
        }
        return null;
    }

    /**
     * Finds a slot that matches an existing appointment start date-time.
     *
     * @param appointment appointment value used by this method
     * @return result produced by this method
     */
    private AppointmentSlot findSlotForAppointment(Appointment appointment) {
        if (appointment == null || appointment.getStartTime() == null) {
            return null;
        }

        for (AppointmentSlot slot : appointmentRepository.findAll()) {
            if (appointment.getStartTime().equals(slot.getDateTime())) {
                return slot;
            }
        }

        return findSlotBySelection(appointment.getSlotDateTimeLabel());
    }

    private void sendAttendedNotificationIfConfigured(Appointment appointment) {
        if (appointmentNotificationCoordinator != null && appointment != null) {
            appointmentNotificationCoordinator.sendAttendedNotification(appointment);
        }
    }

    private void sendCompletedNotificationIfConfigured(Appointment appointment) {
        if (appointmentNotificationCoordinator != null && appointment != null) {
            appointmentNotificationCoordinator.sendCompletedNotification(appointment);
        }
    }

    private void sendNotAttendedNotificationIfConfigured(Appointment appointment) {
        if (appointmentNotificationCoordinator != null && appointment != null) {
            appointmentNotificationCoordinator.sendNotAttendedNotification(appointment);
        }
    }

    private void sendWaitlistPromotionNotificationIfConfigured(Appointment appointment) {
        if (appointmentNotificationCoordinator != null && appointment != null) {
            appointmentNotificationCoordinator.sendWaitlistPromotionNotification(appointment);
        }
    }

    private BookingStatus addToWaitlistForFullSlot(
            AppointmentSlot slot,
            String bookingCustomerName,
            String bookingCustomerEmail,
            String normalizedPhone,
            int durationMinutes,
            int participantCount,
            AppointmentType normalizedType
    ) {
        if (slot == null || slot.getDateTime() == null || waitlistRepository == null) {
            return BookingStatus.SLOT_ALREADY_BOOKED;
        }

        if (hasActiveAppointmentForSlot(slot, bookingCustomerName, bookingCustomerEmail)) {
            return BookingStatus.WAITLIST_ALREADY_BOOKED;
        }

        if (waitlistRepository.existsForSlotAndCustomer(slot.getDateTime(), bookingCustomerName, bookingCustomerEmail)) {
            return BookingStatus.WAITLIST_ALREADY_EXISTS;
        }

        WaitlistEntry waitlistEntry = new WaitlistEntry(
                UUID.randomUUID().toString(),
                bookingCustomerName,
                bookingCustomerEmail,
                normalizedPhone,
                slot.getDateTime(),
                durationMinutes,
                participantCount,
                normalizedType,
                LocalDateTime.now()
        );
        waitlistRepository.save(waitlistEntry);
        return BookingStatus.WAITLISTED;
    }

    private BookingStatus promoteWaitlistedAppointmentIfAvailable(AppointmentSlot slot) {
        if (waitlistRepository == null || slot == null || slot.getDateTime() == null) {
            return BookingStatus.SUCCESS;
        }

        WaitlistEntry nextEntry = waitlistRepository.pollFirstBySlotDateTime(slot.getDateTime()).orElse(null);
        if (nextEntry == null) {
            return BookingStatus.SUCCESS;
        }

        Appointment promoted = nextEntry.toAppointment(AppointmentStatus.CONFIRMED);
        try {
            appointmentBookingRepository.save(promoted);
            slot.book();
        } catch (RuntimeException ex) {
            waitlistRepository.saveFirst(nextEntry);
            return BookingStatus.WAITLIST_PROMOTION_FAILED;
        }

        notifyEvent("Waitlist promotion confirmed: " + promoted.getId());
        sendWaitlistPromotionNotificationIfConfigured(promoted);
        return BookingStatus.SUCCESS;
    }

    private boolean hasActiveAppointmentForSlot(
            AppointmentSlot slot,
            String bookingCustomerName,
            String bookingCustomerEmail
    ) {
        if (slot == null || slot.getDateTime() == null) {
            return false;
        }

        String requestedIdentity = customerIdentity(bookingCustomerName, bookingCustomerEmail);
        if (requestedIdentity == null) {
            return false;
        }

        for (Appointment appointment : appointmentBookingRepository.findAll()) {
            if (!slot.getDateTime().equals(appointment.getStartTime())) {
                continue;
            }

            if (!isActiveBookingStatus(appointment.getStatus())) {
                continue;
            }

            if (requestedIdentity.equalsIgnoreCase(customerIdentity(appointment.getCustomerName(), appointment.getUser() == null ? null : appointment.getUser().getEmail()))) {
                return true;
            }
        }

        return false;
    }

    private boolean isActiveBookingStatus(AppointmentStatus status) {
        return status == AppointmentStatus.CONFIRMED
                || status == AppointmentStatus.MODIFIED
                || status == AppointmentStatus.RESCHEDULED
                || status == AppointmentStatus.ATTENDED;
    }

    private String customerIdentity(String customerName, String customerEmail) {
        String normalizedEmail = normalize(customerEmail);
        if (normalizedEmail != null) {
            return normalizedEmail.toLowerCase();
        }

        String normalizedName = normalize(customerName);
        return normalizedName == null ? null : normalizedName.toLowerCase();
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
        String ownerEmail = appointment.getUser() == null ? null : normalize(appointment.getUser().getEmail());
        return (ownerIdentity != null && ownerIdentity.equalsIgnoreCase(currentEmail))
                || (ownerEmail != null && ownerEmail.equalsIgnoreCase(currentEmail));
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
            try {
                appointmentNotificationCoordinator.sendPendingNotification(appointment);
            } catch (Exception e) {
                System.out.println("Email failed but booking continues: " + e.getMessage());
            }
        }
    }

    /**
     * Sends an approved notification when the coordinator is configured.
     *
     * @param appointment appointment involved in this action
     */
    private void sendApprovedNotificationIfConfigured(Appointment appointment) {
        if (appointmentNotificationCoordinator != null && appointment != null) {
            try {
                appointmentNotificationCoordinator.sendApprovedNotification(appointment);
            } catch (Exception e) {
                System.out.println("Email failed but booking continues: " + e.getMessage());
            }
        }
    }

    /**
     * Sends a cancelled notification when the coordinator is configured.
     *
     * @param appointment appointment involved in this action
     */
    private void sendCancelledNotificationIfConfigured(Appointment appointment) {
        if (appointmentNotificationCoordinator != null && appointment != null) {
            try {
                appointmentNotificationCoordinator.sendCancelledNotification(appointment);
            } catch (Exception e) {
                System.out.println("Email failed but booking continues: " + e.getMessage());
            }
        }
    }

    /**
     * Sends a modified notification when the coordinator is configured.
     *
     * @param previousAppointment appointment details before the change
     * @param updatedAppointment appointment details after the change
     */
    private void sendRescheduledNotificationIfConfigured(Appointment previousAppointment, Appointment updatedAppointment) {
        if (appointmentNotificationCoordinator != null && previousAppointment != null && updatedAppointment != null) {
            try {
                appointmentNotificationCoordinator.sendRescheduledNotification(previousAppointment, updatedAppointment);
            } catch (Exception e) {
                System.out.println("Email failed but booking continues: " + e.getMessage());
            }
        }
    }

    /**
     * Trims text and returns null when the value is blank.
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
     * Trims feedback text and returns null when the value is blank.
     *
     * @param value value used by this method
     * @return text result from this method
     */
    private String normalizeFeedbackComment(String value) {
        return normalize(value);
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        String normalizedPhone = normalize(phoneNumber);
        return normalizedPhone != null && normalizedPhone.matches(PHONE_PATTERN);
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
     * Builds a temporary appointment used for rule checks.
     *
     * @param durationMinutes appointment duration in minutes
     * @param participantCount number of people for the appointment
     * @return result produced by this method
     */
    private Appointment validationProbe(int durationMinutes, int participantCount) {
        return new Appointment("validation", LocalDateTime.now(), durationMinutes, participantCount);
    }

    /**
     * Checks whether the selected appointment type rule accepts this appointment.
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
     * Returns NORMAL when appointment type is null.
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