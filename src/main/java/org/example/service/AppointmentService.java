package org.example.service;

import org.example.domain.AppointmentSlot;

import java.util.ArrayList;
import java.util.List;

public class AppointmentService {

    private List<AppointmentSlot> slots = new ArrayList<>();

    public AppointmentService() {
        slots.add(new AppointmentSlot("10:00"));
        slots.add(new AppointmentSlot("11:00"));
        slots.add(new AppointmentSlot("12:00"));
    }

    public List<AppointmentSlot> getAvailableSlots() {

        List<AppointmentSlot> available = new ArrayList<>();

        for (AppointmentSlot slot : slots) {
            if (!slot.isBooked()) {
                available.add(slot);
            }
        }

        return available;
    }
}