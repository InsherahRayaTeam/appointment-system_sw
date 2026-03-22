package org.example.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class Appointment {

    private final String id;
    private final LocalDateTime startTime;
    private final int duration;
    private final int participants;

    // Optional fields used by booking workflow compatibility.
    private final String customerName;
    private final AppointmentStatus status;

    public Appointment(String id, LocalDateTime startTime, int duration, int participants) {
        this.id = id;
        this.startTime = startTime;
        this.duration = duration;
        this.participants = participants;
        this.customerName = null;
        this.status = null;
    }

    /**
     * Backward-compatible constructor used by booking flow (enum status).
     */
    public Appointment(String customerName, String slotTime, int duration, int participants, AppointmentStatus status) {
        this.id = null;
        this.startTime = parseSlotTime(slotTime);
        this.duration = duration;
        this.participants = participants;
        this.customerName = customerName;
        this.status = status;
    }

    /**
     * Backward-compatible constructor used by legacy callers (String status).
     */
    public Appointment(String customerName, String slotTime, int duration, int participants, String status) {
        this(customerName, slotTime, duration, participants, parseStatus(status));
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public int getDuration() {
        return duration;
    }

    public int getParticipants() {
        return participants;
    }

    public int getDurationMinutes() {
        return duration;
    }

    public int getParticipantCount() {
        return participants;
    }

    public String getSlotTime() {
        return startTime != null ? startTime.toLocalTime().toString() : null;
    }

    public String getCustomerName() {
        return customerName;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    /**
     * Legacy helper that returns the status as text when needed.
     */
    public String getStatusValue() {
        return status == null ? null : status.name();
    }

    private static LocalDateTime parseSlotTime(String slotTime) {
        if (slotTime == null || slotTime.trim().isEmpty()) {
            return null;
        }
        try {
            LocalTime parsedTime = LocalTime.parse(slotTime.trim());
            return LocalDate.now().atTime(parsedTime);
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