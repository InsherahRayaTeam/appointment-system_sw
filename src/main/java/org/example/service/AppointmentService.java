package org.example.service;

import org.example.domain.AppointmentSlot;
import org.example.repository.AppointmentRepository;
import org.example.repository.InMemoryAppointmentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentService() {
        this(new InMemoryAppointmentRepository());
    }

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = Objects.requireNonNull(appointmentRepository, "appointmentRepository cannot be null");
    }

    public List<AppointmentSlot> getAvailableSlots() {
        List<AppointmentSlot> available = new ArrayList<>();
        for (AppointmentSlot slot : appointmentRepository.findAll()) {
            if (slot.isAvailable()) {
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
        for (AppointmentSlot slot : appointmentRepository.findAll()) {
            if (slot.getTime().equals(normalizedTime) && slot.isAvailable()) {
                slot.book();
                return true;
            }
        }
        return false;
    }
}