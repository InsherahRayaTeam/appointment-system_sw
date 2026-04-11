package org.example.service;

import org.example.domain.Appointment;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * Exports appointments to iCalendar (.ics) files.
 */
public class AppointmentCalendarExportService {

    private static final DateTimeFormatter ICS_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final int DEFAULT_DURATION_MINUTES = 60;

    /**
     * Generates an ICS file for the provided appointment.
     *
     * @param appointment appointment to export
     * @return generated ICS file
     * @throws IOException if writing the file fails
     */
    public File generateCalendarFile(Appointment appointment) throws IOException {
        Objects.requireNonNull(appointment, "appointment cannot be null");
        String content = buildIcsContent(appointment);

        String fileName = "appointment-" + sanitizeFileName(appointment.getId()) + ".ics";
        File file = new File(System.getProperty("java.io.tmpdir"), fileName);
        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        return file;
    }

    /**
     * Builds iCalendar text content for a single appointment event.
     *
     * @param appointment appointment to export
     * @return ICS file content text
     */
    public String buildIcsContent(Appointment appointment) {
        Objects.requireNonNull(appointment, "appointment cannot be null");

        LocalDateTime start = appointment.getStartTime();
        if (start == null) {
            throw new IllegalArgumentException("Appointment start time is required for calendar export");
        }

        int durationMinutes = appointment.getDurationMinutes() > 0
                ? appointment.getDurationMinutes()
                : DEFAULT_DURATION_MINUTES;
        LocalDateTime end = start.plusMinutes(durationMinutes);

        String summary = escapeIcsText("Appointment - " + appointment.getType());
        String description = escapeIcsText(buildDescription(appointment));

        return "BEGIN:VCALENDAR\r\n"
                + "VERSION:2.0\r\n"
                + "PRODID:-//Appointment System//EN\r\n"
                + "BEGIN:VEVENT\r\n"
                + "UID:" + buildUid(appointment) + "\r\n"
                + "DTSTAMP:" + LocalDateTime.now().format(ICS_DATE_TIME) + "\r\n"
                + "DTSTART:" + start.format(ICS_DATE_TIME) + "\r\n"
                + "DTEND:" + end.format(ICS_DATE_TIME) + "\r\n"
                + "SUMMARY:" + summary + "\r\n"
                + "DESCRIPTION:" + description + "\r\n"
                + "END:VEVENT\r\n"
                + "END:VCALENDAR\r\n";
    }

    private String buildDescription(Appointment appointment) {
        return "Customer: " + safeValue(appointment.getCustomerName())
                + "\\nStatus: " + safeValue(appointment.getStatus())
                + "\\nType: " + safeValue(appointment.getType())
                + "\\nDate/Time: " + safeValue(appointment.getSlotDateTimeLabel());
    }

    private String buildUid(Appointment appointment) {
        String baseId = sanitizeFileName(appointment.getId());
        if (baseId.isEmpty()) {
            baseId = UUID.randomUUID().toString();
        }
        return baseId + "@appointment-system";
    }

    private String safeValue(Object value) {
        return value == null ? "N/A" : value.toString();
    }

    private String sanitizeFileName(String raw) {
        if (raw == null) {
            return "appointment";
        }
        return raw.replaceAll("[^a-zA-Z0-9-_]", "_");
    }

    private String escapeIcsText(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n");
    }
}

