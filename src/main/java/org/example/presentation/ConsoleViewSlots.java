package org.example.presentation;

import org.example.service.AppointmentService;
import org.example.domain.AppointmentSlot;

public class ConsoleViewSlots {

    private AppointmentService service;

    public ConsoleViewSlots(AppointmentService service) {
        this.service = service;
    }

    public void show() {

        System.out.println("Available appointment slots:");

        for (AppointmentSlot slot : service.getAvailableSlots()) {
            System.out.println(slot.getTime());
        }
    }
}