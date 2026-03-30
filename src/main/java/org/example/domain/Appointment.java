package org.example.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.UUID;

/**
 * Represents an appointment aggregate used by both legacy and booking workflows.
 *
 * @author appointment-system
 * @version 1.0
 */
public class Appointment {

    private final String id;
    private final LocalDateTime startTime;
    private final int duration;
    private final int participants;

    // Optional fields used by booking workflow compatibility.
    private final String customerName;
    private final AppointmentStatus status;

    /**
     * Creates an appointment with explicit identifier and start-time fields.
     *
     * @param id appointment identifier
     * @param startTime appointment start timestamp
     * @param duration appointment duration in minutes
     * @param participants participant count
     */
    public Appointment(String id, LocalDateTime startTime, int duration, int participants) {
        this(id, null, startTime, duration, participants, null);
    }

    /**
     * Creates a complete appointment object with all booking fields.
     *
     * @param id appointment identifier
     * @param customerName customer name
     * @param startTime appointment start timestamp
     * @param duration appointment duration in minutes
     * @param participants participant count
     * @param status appointment status
     */
    public Appointment(
            String id,
            String customerName,
            LocalDateTime startTime,
            int duration,
            int participants,
            AppointmentStatus status
    ) {
        this.id = id;
        this.startTime = startTime;
        this.duration = duration;
        this.participants = participants;
        this.customerName = customerName;
        this.status = status;
    }

    /**
     * Backward-compatible constructor used by booking flow (enum status).
     *
     * @param customerName customer name
     * @param slotTime slot time text representation
     * @param duration appointment duration in minutes
     * @param participants participant count
     * @param status appointment status
     */
    public Appointment(String customerName, String slotTime, int duration, int participants, AppointmentStatus status) {
        this(UUID.randomUUID().toString(), customerName, parseSlotTime(slotTime), duration, participants, status);
    }

    /**
     * Backward-compatible constructor used by legacy callers (String status).
     *
     * @param customerName customer name
     * @param slotTime slot time text representation
     * @param duration appointment duration in minutes
     * @param participants participant count
     * @param status status text representation
     */
    public Appointment(String customerName, String slotTime, int duration, int participants, String status) {
        this(customerName, slotTime, duration, participants, parseStatus(status));
    }

    /**
     * Returns appointment identifier.
     *
     * @return appointment identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns start timestamp.
     *
     * @return appointment start time
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Returns duration in minutes.
     *
     * @return duration in minutes
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Returns participant count.
     *
     * @return participant count
     */
    public int getParticipants() {
        return participants;
    }

    /**
     * Returns duration in minutes.
     *
     * @return duration in minutes
     */
    public int getDurationMinutes() {
        return duration;
    }

    /**
     * Returns participant count.
     *
     * @return participant count
     */
    public int getParticipantCount() {
        return participants;
    }

    /**
     * Returns slot time in local-time string format.
     *
     * @return slot time string or null when unavailable
     */
    public String getSlotTime() {
        return startTime != null ? startTime.toLocalTime().toString() : null;
    }

    /**
     * Returns customer name for booking workflow.
     *
     * @return customer name
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Returns status enum.
     *
     * @return appointment status
     */
    public AppointmentStatus getStatus() {
        return status;
    }

    /**
     * Legacy helper that returns the status as text when needed.
     *
     * @return status value as text, or null when status is unavailable
     */
    public String getStatusValue() {
        return status == null ? null : status.name();
    }

    /**
     * Indicates whether the appointment starts after a given reference time.
     *
     * @param referenceTime timestamp used for comparison
     * @return true when start time exists and is strictly after reference time
     */
    public boolean isFutureComparedTo(LocalDateTime referenceTime) {
        return startTime != null
                && referenceTime != null
                && startTime.isAfter(referenceTime);
    }

    /**
     * Returns a copy with a different status.
     *
     * @param newStatus replacement status
     * @return copied appointment with updated status
     */
    public Appointment withStatus(AppointmentStatus newStatus) {
        return new Appointment(id, customerName, startTime, duration, participants, newStatus);
    }

    /**
     * Returns a copy with a different slot time and status.
     *
     * @param slotTime replacement slot time text
     * @param newStatus replacement status
     * @return copied appointment with updated start time and status
     */
    public Appointment withSlotTimeAndStatus(String slotTime, AppointmentStatus newStatus) {
        return new Appointment(
                id,
                customerName,
                parseSlotTime(slotTime),
                duration,
                participants,
                newStatus
        );
    }

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