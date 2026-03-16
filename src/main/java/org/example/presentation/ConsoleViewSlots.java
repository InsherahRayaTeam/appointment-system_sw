package org.example.presentation;

import org.example.service.AppointmentService;
import org.example.domain.AppointmentSlot;

import java.util.List;
import java.util.Scanner;

public class ConsoleViewSlots {

    private final AppointmentService service;

    public ConsoleViewSlots(AppointmentService service) {
        this.service = service;
    }

    public void show() {
        System.out.println("Available appointment slots:");
        for (AppointmentSlot slot : service.getAvailableSlots()) {
            System.out.println("  " + slot.getTime());
        }
    }

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

