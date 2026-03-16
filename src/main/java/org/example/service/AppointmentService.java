package org.example.service;

import org.example.domain.AppointmentSlot;

import java.util.ArrayList;
import java.util.List;

public class AppointmentService {

    private final List<AppointmentSlot> slots = new ArrayList<>();

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

    /**
     * Books the slot with the given time. Returns true if the slot was found
     * and successfully booked, false if it does not exist or is already booked.
     */
    public boolean bookSlot(String time) {
        if (time == null || time.trim().isEmpty()) {
            return false;
        }
        String normalizedTime = time.trim();
        for (AppointmentSlot slot : slots) {
            if (slot.getTime().equals(normalizedTime) && !slot.isBooked()) {
                slot.book();
                return true;
            }
        }
        return false;
    }
}