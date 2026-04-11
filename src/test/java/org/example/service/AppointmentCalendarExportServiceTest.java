package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentStatus;
import org.example.domain.AppointmentType;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AppointmentCalendarExportServiceTest {

    @Test
    void buildIcsContent_containsRequiredCalendarSectionsAndFields() {
        Appointment appointment = new Appointment(
                "res-123",
                "user@example.com",
                "user@example.com",
                LocalDateTime.of(2030, 12, 12, 10, 0),
                60,
                1,
                AppointmentStatus.CONFIRMED,
                AppointmentType.VIRTUAL
        );

        AppointmentCalendarExportService service = new AppointmentCalendarExportService();
        String content = service.buildIcsContent(appointment);

        assertTrue(content.contains("BEGIN:VCALENDAR"));
        assertTrue(content.contains("BEGIN:VEVENT"));
        assertTrue(content.contains("DTSTART:20301212T100000"));
        assertTrue(content.contains("DTEND:20301212T110000"));
        assertTrue(content.contains("SUMMARY:Appointment - Virtual"));
        assertTrue(content.contains("UID:"));
    }

    @Test
    void generateCalendarFile_writesIcsFileWithExpectedMarkers() throws IOException {
        Appointment appointment = new Appointment(
                "res-999",
                "user@example.com",
                "user@example.com",
                LocalDateTime.of(2031, 1, 1, 9, 30),
                30,
                1,
                AppointmentStatus.CONFIRMED,
                AppointmentType.NORMAL
        );

        AppointmentCalendarExportService service = new AppointmentCalendarExportService();
        File file = service.generateCalendarFile(appointment);
        String fileContent = Files.readString(file.toPath(), StandardCharsets.UTF_8);

        assertTrue(file.exists());
        assertTrue(file.getName().endsWith(".ics"));
        assertTrue(fileContent.contains("BEGIN:VCALENDAR"));
        assertTrue(fileContent.contains("END:VCALENDAR"));
    }
}

