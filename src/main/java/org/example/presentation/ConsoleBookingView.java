package org.example.presentation;

import org.example.domain.AppointmentSlot;

import java.util.List;

/**
 * Renders booking-related console output.
 *
 * @author appointment-system
 * @version 1.0
 */
public class ConsoleBookingView {

    /**
     * Displays available appointment slots as a numbered list.
     *
     * @param slots available slots to render
     */
    public void showSlots(List<AppointmentSlot> slots) {
        printSectionHeader("Available Appointment Slots");
        if (slots == null || slots.isEmpty()) {
            System.out.println("No appointment slots are currently available.");
            printSeparator();
            return;
        }

        int index = 1;
        for (AppointmentSlot slot : slots) {
            System.out.println(index + ". " + slot.getTime());
            index++;
        }
        printSeparator();
    }

    /**
     * Displays booking outcome feedback.
     *
     * @param slotTime requested slot identifier
     * @param booked booking success indicator
     */
    public void showBookingResult(String slotTime, boolean booked) {
        if (booked) {
            System.out.println("Success: Appointment booked for " + slotTime + ".");
        } else {
            System.out.println("Unable to book slot '" + slotTime + "'. It may be invalid or already booked.");
        }
    }

    /**
     * Displays a message when there are no slots to book.
     */
    public void showNoSlotsToBook() {
        System.out.println("There are no available slots to book right now.");
    }

    /**
     * Prints a titled section header.
     *
     * @param title section title
     */
    public void printSectionHeader(String title) {
        printSeparator();
        System.out.println(title);
        printSeparator();
    }

    /**
     * Prints a standard console separator line.
     */
    public void printSeparator() {
        System.out.println("----------------------------------------");
    }
}
