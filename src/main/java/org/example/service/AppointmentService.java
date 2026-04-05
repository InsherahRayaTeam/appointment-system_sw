package org.example.service;

import org.example.domain.AppointmentSlot;
import org.example.repository.AppointmentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Provides appointment-slot operations such as availability lookup, booking, and reminders.
 *
 * @author appointment-system
 * @version 1.0
 */
public class AppointmentService {

    private final List<AppointmentSlot> slots = new ArrayList<>();
    private final EventManager eventManager;


    /**
     * Creates an appointment service using repository data and an event dispatcher.
     *
     * @param appointmentRepository repository source of appointment slots
     * @param eventManager event dispatcher for booking/reminder notifications
     */
    public AppointmentService(AppointmentRepository appointmentRepository, EventManager eventManager) {
        AppointmentRepository repository = Objects.requireNonNull(
                appointmentRepository,
                "appointmentRepository cannot be null"
        );
        this.eventManager = Objects.requireNonNull(eventManager, "eventManager cannot be null");
        this.slots.addAll(repository.findAll());
    }

    /**
     * Returns all currently available (not booked) slots.
     *
     * @return available appointment slots
     */
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
     *
     * @param time slot time identifier
     * @return true on successful booking, otherwise false
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

    /**
     * Backward-compatible reminder API that delegates to {@link #sendReminderForSlot(String)}.
     *
     * @param time slot time identifier
     */
    public void sendReminder(String time) {
        sendReminderForSlot(time);
    }

    /**
     * Sends a reminder for a booked slot.
     *
     * @param time slot time identifier
     * @return true when reminder was sent, otherwise false
     */
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

    /**
     * Sends reminders for all booked slots.
     *
     * @return number of reminders sent
     */
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