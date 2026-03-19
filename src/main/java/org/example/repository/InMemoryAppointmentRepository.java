package org.example.repository;

import org.example.domain.AppointmentSlot;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAppointmentRepository implements AppointmentRepository {

    private final List<AppointmentSlot> slots = new ArrayList<>();

    public InMemoryAppointmentRepository() {
        slots.add(new AppointmentSlot("10:00"));
        slots.add(new AppointmentSlot("11:00"));
        slots.add(new AppointmentSlot("12:00"));
    }

    @Override
    public List<AppointmentSlot> findAll() {
        return new ArrayList<>(slots);
    }

    @Override
    public List<AppointmentSlot> findAvailable() {
        List<AppointmentSlot> available = new ArrayList<>();
        for (AppointmentSlot slot : slots) {
            if (slot.isAvailable()) {
                available.add(slot);
            }
        }
        return available;
    }
}

