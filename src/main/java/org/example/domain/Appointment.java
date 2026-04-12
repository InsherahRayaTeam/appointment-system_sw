package org.example.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.UUID;

/**
 * Represents appointment in the system.
 */
public class Appointment {

    private static final DateTimeFormatter SLOT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String id;
    private final LocalDateTime startTime;
    private final int duration;
    private final int participants;
    private AppointmentType type;
    private int rating;
    private String feedbackComment;
    private boolean feedbackSubmitted;

    // Optional fields used by booking workflow compatibility.
    private final String customerName;
    private final String customerEmail;
    private final String customerPhoneNumber;
    private final AppointmentStatus status;

    /**
     * Creates a new appointment object with the given values.
     *
     * @param id unique id used to find the record
     * @param startTime time value used by this method
     * @param duration appointment duration in minutes
     * @param participants number of people for the appointment
     */
    public Appointment(String id, LocalDateTime startTime, int duration, int participants) {
        this(id, null, null, startTime, duration, participants, null, AppointmentType.NORMAL);
    }

    /**
     * Creates a new appointment object with the given values.
     *
     * @param id unique id used to find the record
     * @param customerName value for customer name
     * @param startTime time value used by this method
     * @param duration appointment duration in minutes
     * @param participants number of people for the appointment
     * @param status status value used for this operation
     */
    public Appointment(
            String id,
            String customerName,
            LocalDateTime startTime,
            int duration,
            int participants,
            AppointmentStatus status
    ) {
        this(id, customerName, customerName, startTime, duration, participants, status, AppointmentType.NORMAL);
    }

    /**
     * Creates a new appointment object with the given values.
     *
     * @param id unique id used to find the record
     * @param customerName value for customer name
     * @param customerEmail value for customer email
     * @param startTime time value used by this method
     * @param duration appointment duration in minutes
     * @param participants number of people for the appointment
     * @param status status value used for this operation
     */
    public Appointment(
            String id,
            String customerName,
            String customerEmail,
            LocalDateTime startTime,
            int duration,
            int participants,
            AppointmentStatus status
    ) {
        this(id, customerName, customerEmail, startTime, duration, participants, status, AppointmentType.NORMAL);
    }

    /**
     * Creates a new appointment object with the given values.
     *
     * @param id unique id used to find the record
     * @param customerName value for customer name
     * @param startTime time value used by this method
     * @param duration appointment duration in minutes
     * @param participants number of people for the appointment
     * @param status status value used for this operation
     * @param type value for type
     */
    public Appointment(
            String id,
            String customerName,
            String customerEmail,
            LocalDateTime startTime,
            int duration,
            int participants,
            AppointmentStatus status,
            AppointmentType type
    ) {
        this(
                id,
                customerName,
                customerEmail,
                null,
                startTime,
                duration,
                participants,
                status,
                type
        );
    }

    /**
     * Creates a new appointment object with the given values.
     *
     * @param id unique id used to find the record
     * @param customerName value for customer name
     * @param customerEmail value for customer email
     * @param customerPhoneNumber value for customer phone number
     * @param startTime time value used by this method
     * @param duration appointment duration in minutes
     * @param participants number of people for the appointment
     * @param status status value used for this operation
     * @param type value for type
     */
    public Appointment(
            String id,
            String customerName,
            String customerEmail,
            String customerPhoneNumber,
            LocalDateTime startTime,
            int duration,
            int participants,
            AppointmentStatus status,
            AppointmentType type
    ) {
        this.id = id;
        this.startTime = startTime;
        this.duration = duration;
        this.participants = participants;
        this.customerName = customerName;
        this.customerEmail = customerEmail == null || customerEmail.trim().isEmpty()
                ? customerName
                : customerEmail.trim().toLowerCase();
        this.customerPhoneNumber = customerPhoneNumber == null || customerPhoneNumber.trim().isEmpty()
                ? null
                : customerPhoneNumber.trim();
        this.status = status;
        this.type = type == null ? AppointmentType.NORMAL : type;
        this.rating = 0;
        this.feedbackComment = null;
        this.feedbackSubmitted = false;
    }

    /**
     * Creates a new appointment object with the given values.
     *
     * @param customerName value for customer name
     * @param slotTime slot time text like 10:00
     * @param duration appointment duration in minutes
     * @param participants number of people for the appointment
     * @param status status value used for this operation
     */
    public Appointment(String customerName, String slotTime, int duration, int participants, AppointmentStatus status) {
        this(
                UUID.randomUUID().toString(),
                customerName,
                customerName,
                parseSlotTime(slotTime),
                duration,
                participants,
                status,
                AppointmentType.NORMAL
        );
    }

    /**
     * Creates a new appointment object with the given values.
     *
     * @param customerName value for customer name
     * @param startTime time value used by this method
     * @param duration appointment duration in minutes
     * @param participants number of people for the appointment
     * @param status status value used for this operation
     * @param type value for type
     */
    public Appointment(
            String customerName,
            LocalDateTime startTime,
            int duration,
            int participants,
            AppointmentStatus status,
            AppointmentType type
    ) {
        this(UUID.randomUUID().toString(), customerName, customerName, startTime, duration, participants, status, type);
    }

    /**
     * Creates a new appointment object with the given values.
     *
     * @param customerName value for customer name
     * @param slotTime slot time text like 10:00
     * @param duration appointment duration in minutes
     * @param participants number of people for the appointment
     * @param status status value used for this operation
     * @param type value for type
     */
    public Appointment(
            String customerName,
            String slotTime,
            int duration,
            int participants,
            AppointmentStatus status,
            AppointmentType type
    ) {
        this(UUID.randomUUID().toString(), customerName, customerName, parseSlotTime(slotTime), duration, participants, status, type);
    }

    /**
     * Creates a new appointment object with the given values.
     *
     * @param customerName value for customer name
     * @param slotTime slot time text like 10:00
     * @param duration appointment duration in minutes
     * @param participants number of people for the appointment
     * @param status status value used for this operation
     */
    public Appointment(String customerName, String slotTime, int duration, int participants, String status) {
        this(customerName, slotTime, duration, participants, parseStatus(status), AppointmentType.NORMAL);
    }

    /**
     * Creates a new appointment object with the given values.
     *
     * @param customerName value for customer name
     * @param slotTime slot time text like 10:00
     * @param duration appointment duration in minutes
     * @param participants number of people for the appointment
     * @param status status value used for this operation
     * @param type value for type
     */
    public Appointment(
            String customerName,
            String slotTime,
            int duration,
            int participants,
            String status,
            AppointmentType type
    ) {
        this(customerName, slotTime, duration, participants, parseStatus(status), type);
    }

    /**
     * Returns the id.
     *
     * @return text result from this method
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the start time.
     *
     * @return requested value from this object
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Returns the duration.
     *
     * @return numeric result from this method
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Returns the participants.
     *
     * @return numeric result from this method
     */
    public int getParticipants() {
        return participants;
    }

    /**
     * Returns the duration minutes.
     *
     * @return numeric result from this method
     */
    public int getDurationMinutes() {
        return duration;
    }

    /**
     * Returns the participant count.
     *
     * @return numeric result from this method
     */
    public int getParticipantCount() {
        return participants;
    }

    /**
     * Returns the slot time.
     *
     * @return text result from this method
     */
    public String getSlotTime() {
        return startTime != null ? startTime.toLocalTime().toString() : null;
    }

    /**
     * Returns the slot date.
     *
     * @return text result from this method
     */
    public String getSlotDate() {
        return startTime != null ? startTime.toLocalDate().toString() : null;
    }

    /**
     * Returns the slot day.
     *
     * @return text result from this method
     */
    public String getSlotDay() {
        return startTime != null ? startTime.getDayOfWeek().toString() : null;
    }

    /**
     * Returns the slot date-time label.
     *
     * @return text result from this method
     */
    public String getSlotDateTimeLabel() {
        return startTime != null ? startTime.format(SLOT_DATE_TIME_FORMATTER) : null;
    }

    /**
     * Returns the customer name.
     *
     * @return text result from this method
     */
    public String getCustomerName() {
        return customerName;
    }


    /**
     * Returns the customer phone number.
     *
     * @return text result from this method
     */
    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }

    /**
     * Returns the user data represented by this appointment.
     *
     * @return user involved in this action
     */
    public SystemUser getUser() {
        String identity = customerEmail != null && !customerEmail.trim().isEmpty() ? customerEmail : customerName;
        if (identity == null || identity.trim().isEmpty()) {
            return null;
        }
        return new SystemUser(identity, identity, "notification-only", UserRole.USER);
    }

    /**
     * Returns the status.
     *
     * @return status that explains the operation result
     */
    public AppointmentStatus getStatus() {
        return status;
    }

    /**
     * Returns the type.
     *
     * @return requested value from this object
     */
    public AppointmentType getType() {
        return type;
    }

    /**
     * Updates the type.
     *
     * @param type value for type
     */
    public void setType(AppointmentType type) {
        this.type = type == null ? AppointmentType.NORMAL : type;
    }


    /**
     * Checks whether future compared to is true.
     *
     * @param referenceTime time value used by this method
     * @return true when the action is valid or successful, otherwise false
     */
    public boolean isFutureComparedTo(LocalDateTime referenceTime) {
        return startTime != null
                && referenceTime != null
                && startTime.isAfter(referenceTime);
    }

    /**
     * Returns a copy of this appointment with a new status.
     *
     * @param newStatus status value used for this operation
     * @return result produced by this method
     */
    public Appointment withStatus(AppointmentStatus newStatus) {
        return copyFeedbackState(new Appointment(
                id,
                customerName,
                customerEmail,
                customerPhoneNumber,
                startTime,
                duration,
                participants,
                newStatus,
                type
        ));
    }

    /**
     * Returns a copy of this appointment with a new phone number.
     *
     * @param newCustomerPhoneNumber phone number text used for this appointment
     * @return result produced by this method
     */
    public Appointment withCustomerPhoneNumber(String newCustomerPhoneNumber) {
        return copyFeedbackState(new Appointment(
                id,
                customerName,
                customerEmail,
                newCustomerPhoneNumber,
                startTime,
                duration,
                participants,
                status,
                type
        ));
    }

    /**
     * Returns a copy of this appointment with a new slot time and status.
     *
     * @param slotTime slot time text like 10:00
     * @param newStatus status value used for this operation
     * @return result produced by this method
     */
    public Appointment withSlotTimeAndStatus(String slotTime, AppointmentStatus newStatus) {
        return copyFeedbackState(new Appointment(
                id,
                customerName,
                customerEmail,
                customerPhoneNumber,
                parseSlotTime(slotTime),
                duration,
                participants,
                newStatus,
                type
        ));
    }

    /**
     * Returns a copy of this appointment with a new start time and status.
     *
     * @param newStartTime time value used by this method
     * @param newStatus status value used for this operation
     * @return result produced by this method
     */
    public Appointment withStartTimeAndStatus(LocalDateTime newStartTime, AppointmentStatus newStatus) {
        return copyFeedbackState(new Appointment(
                id,
                customerName,
                customerEmail,
                customerPhoneNumber,
                newStartTime,
                duration,
                participants,
                newStatus,
                type
        ));
    }

    /**
     * Returns the feedback rating.
     *
     * @return rating value stored in this appointment
     */
    public int getRating() {
        return rating;
    }

    /**
     * Updates the feedback rating.
     *
     * @param rating rating value to store
     */
    public void setRating(int rating) {
        this.rating = rating;
    }

    /**
     * Returns the feedback comment.
     *
     * @return comment text stored in this appointment
     */
    public String getFeedbackComment() {
        return feedbackComment;
    }

    /**
     * Updates the feedback comment.
     *
     * @param feedbackComment comment text to store
     */
    public void setFeedbackComment(String feedbackComment) {
        this.feedbackComment = feedbackComment == null || feedbackComment.trim().isEmpty()
                ? null
                : feedbackComment.trim();
    }

    /**
     * Checks whether feedback was already submitted.
     *
     * @return true when feedback exists for this appointment
     */
    public boolean isFeedbackSubmitted() {
        return feedbackSubmitted;
    }

    /**
     * Updates the feedback submitted flag.
     *
     * @param feedbackSubmitted flag that says if feedback was saved
     */
    public void setFeedbackSubmitted(boolean feedbackSubmitted) {
        this.feedbackSubmitted = feedbackSubmitted;
    }

    /**
     * Parses slot time into a future local date-time.
     *
     * @param slotTime slot time text like 10:00
     * @return parsed future date-time, or null if invalid
     */
    private static LocalDateTime parseSlotTime(String slotTime) {
        if (slotTime == null || slotTime.trim().isEmpty()) {
            return null;
        }

        String normalized = slotTime.trim();

        LocalDateTime parsedDateTime = tryParseDateTime(normalized);
        if (parsedDateTime != null) {
            return parsedDateTime;
        }

        try {
            LocalTime parsedTime = LocalTime.parse(normalized);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime candidate = LocalDate.now().atTime(parsedTime);

            if (!candidate.isAfter(now)) {
                candidate = candidate.plusDays(1);
            }

            return candidate;
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private static LocalDateTime tryParseDateTime(String value) {
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
            // Fallback to custom formats below.
        }

        try {
            return LocalDateTime.parse(value, SLOT_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ignored) {
            // Fallback for display formats.
        }

        if (value.length() >= 16) {
            String candidate = value.substring(0, 10) + " " + value.substring(value.length() - 5);
            try {
                return LocalDateTime.parse(candidate, SLOT_DATE_TIME_FORMATTER);
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }

        return null;
    }

    private Appointment copyFeedbackState(Appointment copy) {
        copy.setRating(rating);
        copy.setFeedbackComment(feedbackComment);
        copy.setFeedbackSubmitted(feedbackSubmitted);
        return copy;
    }

    /**
     * Parses a text status into appointment status enum.
     *
     * @param status raw status text
     * @return parsed status, or null if invalid
     */
    private static AppointmentStatus parseStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }

        try {
            return AppointmentStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}