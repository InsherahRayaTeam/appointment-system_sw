package org.example.presentation;

import org.example.service.AppointmentService;
import org.example.domain.AppointmentSlot;

import java.util.List;
import java.util.Scanner;

/**
 * Handles console-based display and interaction for appointment slots.
 * Displays available slots and manages slot booking through user input.
 */
public class ConsoleViewSlots {

    private final AppointmentService service;

    /**
     * Creates a console view for appointment slots.
     *
     * @param service the appointment service providing slot data
     */
    public ConsoleViewSlots(AppointmentService service) {
        this.service = service;
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
}

