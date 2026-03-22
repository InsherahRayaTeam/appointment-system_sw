package org.example.presentation;

import org.example.domain.AppointmentSlot;
import org.example.service.AppointmentBookingService;
import org.example.service.AppointmentService;
import org.example.service.BookingStatus;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConsoleViewSlotsTest {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private AppointmentBookingService bookingService;

    private ConsoleViewSlots consoleViewSlots;
    private AutoCloseable mocks;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        consoleViewSlots = new ConsoleViewSlots(appointmentService);
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
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
        assertTrue(output.contains("Available Appointment Slots"));
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

    @Test
    void bookAppointment_SuccessStatus_PrintsSuccessMessage() {
        ConsoleViewSlots viewSlots = new ConsoleViewSlots(appointmentService, bookingService);
        when(bookingService.bookAppointment("Alice", "10:00", "60", "2"))
                .thenReturn(BookingStatus.SUCCESS);

        viewSlots.bookAppointment(new Scanner("Alice\n10:00\n60\n2\n"));

        String output = capturedOutput();
         assertTrue(output.contains("Booking confirmed successfully."));
        verify(bookingService).bookAppointment(eq("Alice"), eq("10:00"), eq("60"), eq("2"));
    }

    @Test
    void bookAppointment_BlankCustomerNameStatus_PrintsSpecificMessage() {
        assertBookingMessageForStatus(
                BookingStatus.BLANK_CUSTOMER_NAME,
                "Booking failed: customer name cannot be blank."
        );
    }

    @Test
    void bookAppointment_BlankSlotTimeStatus_PrintsSpecificMessage() {
        assertBookingMessageForStatus(
                BookingStatus.BLANK_SLOT_TIME,
                "Booking failed: slot time cannot be blank."
        );
    }

    @Test
    void bookAppointment_InvalidDurationStatus_PrintsSpecificMessage() {
        assertBookingMessageForStatus(
                BookingStatus.INVALID_DURATION,
                "Booking failed: duration exceeds the allowed maximum (120 minutes)."
        );
    }

    @Test
    void bookAppointment_InvalidParticipantCountStatus_PrintsSpecificMessage() {
        assertBookingMessageForStatus(
                BookingStatus.INVALID_PARTICIPANT_COUNT,
                "Booking failed: participant count exceeds the allowed maximum (5)."
        );
    }

    @Test
    void bookAppointment_SlotNotFoundStatus_PrintsSpecificMessage() {
        assertBookingMessageForStatus(
                BookingStatus.SLOT_NOT_FOUND,
                "Booking failed: selected slot does not exist."
        );
    }

    @Test
    void bookAppointment_SlotAlreadyBookedStatus_PrintsSpecificMessage() {
        assertBookingMessageForStatus(
                BookingStatus.SLOT_ALREADY_BOOKED,
                "Booking failed: selected slot is already booked."
        );
    }

    private void assertBookingMessageForStatus(BookingStatus status, String expectedMessage) {
        ConsoleViewSlots viewSlots = new ConsoleViewSlots(appointmentService, bookingService);
        when(bookingService.bookAppointment("Alice", "10:00", "60", "2")).thenReturn(status);

        viewSlots.bookAppointment(new Scanner("Alice\n10:00\n60\n2\n"));

        String output = capturedOutput();
        assertTrue(output.contains(expectedMessage));
        verify(bookingService).bookAppointment(eq("Alice"), eq("10:00"), eq("60"), eq("2"));
    }

    private String capturedOutput() {
        return outputStream.toString(StandardCharsets.UTF_8);
    }
}

