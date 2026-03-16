package org.example.presentation;

import org.example.domain.AppointmentSlot;
import org.example.service.AppointmentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class ConsoleViewSlotsTest {

    @Mock
    private AppointmentService appointmentService;

    private ConsoleViewSlots consoleViewSlots;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        consoleViewSlots = new ConsoleViewSlots(appointmentService);
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void show_PrintsAvailableSlots() {
        List<AppointmentSlot> slots = new ArrayList<>();
        slots.add(new AppointmentSlot("10:00"));
        slots.add(new AppointmentSlot("11:00"));
        slots.add(new AppointmentSlot("12:00"));

        when(appointmentService.getAvailableSlots()).thenReturn(slots);

        consoleViewSlots.show();

        String output = capturedOutput();
        assertTrue(output.contains("Available appointment slots:"));
        assertTrue(output.contains("10:00"));
        assertTrue(output.contains("11:00"));
        assertTrue(output.contains("12:00"));
    }

    @Test
    void bookSlot_NoAvailableSlots_PrintsMessage() {
        when(appointmentService.getAvailableSlots()).thenReturn(new ArrayList<>());

        consoleViewSlots.bookSlot(new Scanner("ignored\n"));

        String output = capturedOutput();
        assertTrue(output.contains("No available appointment slots to book."));
    }

    @Test
    void bookSlot_SuccessfulBooking_PrintsSuccessMessage() {
        List<AppointmentSlot> slots = new ArrayList<>();
        slots.add(new AppointmentSlot("10:00"));
        slots.add(new AppointmentSlot("11:00"));

        when(appointmentService.getAvailableSlots()).thenReturn(slots);
        when(appointmentService.bookSlot("10:00")).thenReturn(true);

        consoleViewSlots.bookSlot(new Scanner("10:00\n"));

        String output = capturedOutput();
        assertTrue(output.contains("Slot 10:00 booked successfully."));
    }

    @Test
    void bookSlot_FailedBooking_PrintsErrorMessage() {
        List<AppointmentSlot> slots = new ArrayList<>();
        slots.add(new AppointmentSlot("10:00"));
        slots.add(new AppointmentSlot("11:00"));

        when(appointmentService.getAvailableSlots()).thenReturn(slots);
        when(appointmentService.bookSlot("09:00")).thenReturn(false);

        consoleViewSlots.bookSlot(new Scanner("09:00\n"));

        String output = capturedOutput();
        assertTrue(output.contains("Could not book slot '09:00'. It may not exist or is already booked."));
    }

    private String capturedOutput() {
        return outputStream.toString(StandardCharsets.UTF_8);
    }
}

