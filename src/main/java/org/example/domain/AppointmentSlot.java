package org.example.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Represents appointment slot in the system.
 */
public class AppointmentSlot {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter COMPACT_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd (EEEE) HH:mm",
            Locale.ENGLISH
    );
    private static final String NOT_AVAILABLE = "N/A";

    private final LocalDate date;
    private final LocalTime time;
    private boolean booked;

    /**
     * Creates a new appointment slot object with the given values.
     *
     * @param time time value used by this method
     */
    public AppointmentSlot(String time) {
        this(LocalDate.now().plusDays(1), parseTime(time));
    }

    /**
     * Creates a new appointment slot object with the given values.
     *
     * @param date slot date
     * @param time slot time
     */
    public AppointmentSlot(LocalDate date, LocalTime time) {
        this.date = date;
        this.time = time;
        this.booked = false;
    }

    /**
     * Returns the time.
     *
     * @return text result from this method
     */
    public String getTime() {
        return time == null ? null : time.format(TIME_FORMATTER);
    }

    /**
     * Returns the date.
     *
     * @return requested value from this object
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns the day value.
     *
     * @return requested value from this object
     */
    public DayOfWeek getDay() {
        return date == null ? null : date.getDayOfWeek();
    }

    /**
     * Returns the slot start date-time.
     *
     * @return requested value from this object
     */
    public LocalDateTime getDateTime() {
        if (date == null || time == null) {
            return null;
        }
        return date.atTime(time);
    }

    /**
     * Returns date/day/time display label.
     *
     * @return text result from this method
     */
    public String getDateDayTimeLabel() {
        LocalDateTime dateTime = getDateTime();
        return dateTime == null ? NOT_AVAILABLE : dateTime.format(DISPLAY_FORMATTER);
    }

    /**
     * Checks whether booked is true.
     *
     * @return true when the action is valid or successful, otherwise false
     */
    public boolean isBooked() {
        return booked;
    }

    /**
     * Checks whether available is true.
     *
     * @return true when the action is valid or successful, otherwise false
     */
    public boolean isAvailable() {
        return !booked;
    }

    /**
     * Checks whether this slot is future based on current time.
     *
     * @return true when the action is valid or successful, otherwise false
     */
    public boolean isFutureSlot() {
        return isFutureComparedTo(LocalDateTime.now());
    }

    /**
     * Checks whether this slot is future compared to reference time.
     *
     * @param referenceTime time value used by this method
     * @return true when the action is valid or successful, otherwise false
     */
    public boolean isFutureComparedTo(LocalDateTime referenceTime) {
        LocalDateTime dateTime = getDateTime();
        return dateTime != null && referenceTime != null && dateTime.isAfter(referenceTime);
    }

    /**
     * Checks whether this slot matches a user selection.
     *
     * @param selection selection text from UI input
     * @return true when the action is valid or successful, otherwise false
     */
    public boolean matchesSelection(String selection) {
        if (selection == null || selection.trim().isEmpty()) {
            return false;
        }

        String normalized = selection.trim();
        if (normalized.equals(getDateDayTimeLabel()) || normalized.equals(getTime())) {
            return true;
        }

        LocalDateTime dateTime = getDateTime();
        if (dateTime == null) {
            return false;
        }

        return normalized.equals(dateTime.toString())
                || normalized.equals(dateTime.format(COMPACT_DISPLAY_FORMATTER));
    }

    /**
     * Runs book for this class.
     */
    public void book() {
        booked = true;
    }

    /**
     * Runs release for this class.
     */
    public void release() {
        booked = false;
    }

    private static LocalTime parseTime(String time) {
        if (time == null || time.trim().isEmpty()) {
            throw new IllegalArgumentException("time cannot be blank");
        }
        try {
            return LocalTime.parse(time.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("time must use HH:mm format", ex);
        }
    }
}
