package org.example.service;

import org.example.domain.AppointmentSlot;
import org.example.repository.AppointmentRepository;
import org.example.repository.InMemoryAppointmentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AppointmentService {

    private final List<AppointmentSlot> slots = new ArrayList<>();
    private final EventManager eventManager;

    public AppointmentService() {
        this(new InMemoryAppointmentRepository(), new EventManager());
    }

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this(appointmentRepository, new EventManager());
    }

    public AppointmentService(AppointmentRepository appointmentRepository, EventManager eventManager) {
        AppointmentRepository repository = Objects.requireNonNull(
                appointmentRepository,
                "appointmentRepository cannot be null"
        );
        this.eventManager = Objects.requireNonNull(eventManager, "eventManager cannot be null");
        this.slots.addAll(repository.findAll());
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

                eventManager.notifyObservers("Appointment booked successfully at " + normalizedTime);

                return true;
            }
        }

        return false;
    }

    public void sendReminder(String time) {
        sendReminderForSlot(time);
    }

    public boolean sendReminderForSlot(String time) {
        if (time == null || time.trim().isEmpty()) {
            return false;
        }

        String normalizedTime = time.trim();
        for (AppointmentSlot slot : slots) {
            if (slot.getTime().equals(normalizedTime) && slot.isBooked()) {
                eventManager.notifyObservers("Reminder: Appointment at " + normalizedTime);
                return true;
            }
        }

        return false;
    }

    public int sendAllReminders() {
        int sentCount = 0;
        for (AppointmentSlot slot : slots) {
            if (slot.isBooked()) {
                eventManager.notifyObservers("Reminder: Appointment at " + slot.getTime());
                sentCount++;
            }
        }
        return sentCount;
    }
}