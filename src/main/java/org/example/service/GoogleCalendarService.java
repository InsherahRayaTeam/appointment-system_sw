package org.example.service;

import org.example.domain.Appointment;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Opens pre-filled Google Calendar events in the browser.
 */
public class GoogleCalendarService {

    private static final DateTimeFormatter GOOGLE_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    /**
     * Opens the Google Calendar event page for one appointment.
     *
     * @param appointment appointment to open in Google Calendar
     */
    public void openGoogleCalendarEvent(Appointment appointment) {
        Objects.requireNonNull(appointment, "appointment cannot be null");

        if (!Desktop.isDesktopSupported()) {
            throw new IllegalStateException("Desktop browsing is not supported on this system");
        }

        String url = buildGoogleCalendarEventUrl(appointment);
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to open Google Calendar in browser", ex);
        }
    }

    /**
     * Builds the Google Calendar URL with appointment details.
     *
     * @param appointment appointment to map to URL parameters
     * @return pre-filled Google Calendar URL
     */
    String buildGoogleCalendarEventUrl(Appointment appointment) {
        Objects.requireNonNull(appointment, "appointment cannot be null");

        LocalDateTime start = appointment.getStartTime();
        if (start == null) {
            throw new IllegalArgumentException("Appointment start time is required");
        }

        int durationMinutes = appointment.getDurationMinutes() > 0 ? appointment.getDurationMinutes() : 60;
        LocalDateTime end = start.plusMinutes(durationMinutes);

        String title = "Appointment with " + safeValue(appointment.getCustomerName());
        String details = "Phone: " + safeValue(appointment.getCustomerPhoneNumber())
                + "\nType: " + safeValue(appointment.getType())
                + "\nParticipants: " + appointment.getParticipantCount();

        return "https://calendar.google.com/calendar/render?action=TEMPLATE"
                + "&text=" + encode(title)
                + "&dates=" + formatDateTime(start) + "/" + formatDateTime(end)
                + "&details=" + encode(details);
    }

    /**
     * Formats LocalDateTime for Google Calendar URLs.
     *
     * @param dateTime date and time to format
     * @return formatted date-time using yyyyMMdd'T'HHmmss
     */
    String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            throw new IllegalArgumentException("dateTime cannot be null");
        }
        return dateTime.format(GOOGLE_DATE_TIME_FORMAT);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String safeValue(Object value) {
        return value == null ? "N/A" : value.toString();
    }
}

