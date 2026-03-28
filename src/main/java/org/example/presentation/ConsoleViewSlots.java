package org.example.presentation;

import org.example.service.AppointmentService;
import org.example.service.AppointmentBookingService;
import org.example.service.BookingStatus;
import org.example.domain.AppointmentSlot;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * Handles console-based display and interaction for appointment slots.
 * Displays available slots and manages slot booking through user input.
 *
 * @author appointment-system
 * @version 1.0
 */
public class ConsoleViewSlots {

    private final AppointmentService service;
    private final AppointmentBookingService bookingService;

    /**
     * Creates a console view for appointment slots.
     *
     * @param service the appointment service providing slot data
     */
    public ConsoleViewSlots(AppointmentService service) {
        this(service, null);
    }

    /**
     * Creates a console view for appointment slots and booking workflow.
     *
     * @param service the appointment service providing slot data
     * @param bookingService the booking service for creating appointments
     */
    public ConsoleViewSlots(AppointmentService service, AppointmentBookingService bookingService) {
        this.service = Objects.requireNonNull(service, "service cannot be null");
        this.bookingService = bookingService;
    }

    /**
     * Displays all available appointment slots to the console.
     */
    public void show() {
        List<AppointmentSlot> slots = service.getAvailableSlots();
        
        System.out.println("========================================");
        System.out.println("     Available Appointment Slots");
        System.out.println("========================================");
        
        if (slots.isEmpty()) {
            System.out.println("No appointment slots currently available.");
        } else {
            for (AppointmentSlot slot : slots) {
                System.out.println("  • " + slot.getTime());
            }
        }
        System.out.println("========================================");
    }

    /**
     * Interactively books an appointment slot selected by the user.
     * Displays available slots and prompts for a selection.
     *
     * @param scanner the input scanner for reading user input
     */
    public void bookSlot(Scanner scanner) {
        List<AppointmentSlot> available = service.getAvailableSlots();
        if (available.isEmpty()) {
            System.out.println("No available appointment slots to book.");
            return;
        }

        System.out.println("Available slots:");
        for (AppointmentSlot slot : available) {
            System.out.println("  " + slot.getTime());
        }

        System.out.print("Enter the time of the slot you want to book: ");
        String time = scanner.nextLine();

        boolean booked = service.bookSlot(time);
        if (booked) {
            System.out.println("Slot " + time.trim() + " booked successfully.");
        } else {
            System.out.println("Could not book slot '" + time.trim() + "'. It may not exist or is already booked.");
        }
    }

    /**
     * Interactively collects booking details and attempts to book an appointment.
     * Delegates booking validation and decision logic to {@link AppointmentBookingService}
     * and prints user-facing feedback based on the returned booking status.
     *
     * @param scanner the input scanner for reading user input
     */
    public void bookAppointment(Scanner scanner) {
        if (bookingService == null) {
            System.out.println("Booking is not available in this context.");
            return;
        }

        System.out.println("========================================");
        System.out.println("           Book Appointment");
        System.out.println("========================================");
        System.out.print("Customer name: ");
        String customerName = scanner.nextLine();

        System.out.print("Slot time (for example, 10:00): ");
        String slotTime = scanner.nextLine();

        System.out.print("Duration in minutes: ");
        String durationMinutes = scanner.nextLine();

        System.out.print("Participant count: ");
        String participantCount = scanner.nextLine();

        BookingStatus status = bookingService.bookAppointment(
                customerName,
                slotTime,
                durationMinutes,
                participantCount
        );

        System.out.println(messageFor(status));
    }

    /**
     * Maps booking outcomes to user-friendly console messages.
     *
     * @param status the booking outcome status returned by the service layer
     * @return the message to display to the user
     */
    private String messageFor(BookingStatus status) {
        switch (status) {
            case SUCCESS:
                return "Booking confirmed successfully.";
            case BLANK_CUSTOMER_NAME:
                return "Booking failed: customer name cannot be blank.";
            case BLANK_SLOT_TIME:
                return "Booking failed: slot time cannot be blank.";
            case INVALID_DURATION:
                return "Booking failed: duration exceeds the allowed maximum (120 minutes).";
            case INVALID_PARTICIPANT_COUNT:
                return "Booking failed: participant count exceeds the allowed maximum (5).";
            case SLOT_NOT_FOUND:
                return "Booking failed: selected slot does not exist.";
            case SLOT_ALREADY_BOOKED:
                return "Booking failed: selected slot is already booked.";
            default:
                return "Booking failed: unexpected booking status.";
        }
    }
}
