package org.example.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.UUID;

/**
 * Represents appointment in the system.
 */
public class Appointment {

    private final String id;
    private final LocalDateTime startTime;
    private final int duration;
    private final int participants;
    private AppointmentType type;

    // Optional fields used by booking workflow compatibility.
    private final String customerName;
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
        this(id, null, startTime, duration, participants, null, AppointmentType.NORMAL);
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
        this(id, customerName, startTime, duration, participants, status, AppointmentType.NORMAL);
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
        this.status = status;
        this.type = type == null ? AppointmentType.NORMAL : type;
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
        this(UUID.randomUUID().toString(), customerName, parseSlotTime(slotTime), duration, participants, status, type);
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
     * Returns the customer name.
     *
     * @return text result from this method
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Returns the user data represented by this appointment.
     *
     * @return user involved in this action
     */
    public SystemUser getUser() {
        if (customerName == null || customerName.trim().isEmpty()) {
            return null;
        }
        return new SystemUser(customerName, "notification-only", UserRole.USER);
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
     * Returns the status value.
     *
     * @return text result from this method
     */
    public String getStatusValue() {
        return status == null ? null : status.name();
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
     * Runs with status for this class.
     *
     * @param newStatus status value used for this operation
     * @return result produced by this method
     */
    public Appointment withStatus(AppointmentStatus newStatus) {
        return new Appointment(id, customerName, startTime, duration, participants, newStatus, type);
    }

    /**
     * Runs with slot time and status for this class.
     *
     * @param slotTime slot time text like 10:00
     * @param newStatus status value used for this operation
     * @return result produced by this method
     */
    public Appointment withSlotTimeAndStatus(String slotTime, AppointmentStatus newStatus) {
        return new Appointment(
                id,
                customerName,
                parseSlotTime(slotTime),
                duration,
                participants,
                newStatus,
                type
        );
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

        try {
            LocalTime parsedTime = LocalTime.parse(slotTime.trim());
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