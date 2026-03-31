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

    private final AppointmentRepository appointmentRepository;
    private final List<AppointmentSlot> slots = new ArrayList<>();
    private final EventManager eventManager;


    /**
     * Creates an appointment service using repository data and an event dispatcher.
     *
     * @param appointmentRepository repository source of appointment slots
     * @param eventManager event dispatcher for booking/reminder notifications
     */
    public AppointmentService(AppointmentRepository appointmentRepository, EventManager eventManager) {
        this.appointmentRepository = Objects.requireNonNull(
                appointmentRepository,
                "appointmentRepository cannot be null"
        );
        this.eventManager = Objects.requireNonNull(eventManager, "eventManager cannot be null");
        this.slots.addAll(appointmentRepository.findAll());
    }

    /**
     * Returns all currently available (not booked or cancelled) slots.
     *
     * @return available appointment slots
     */
    public List<AppointmentSlot> getAvailableSlots() {
        List<AppointmentSlot> available = new ArrayList<>();
        for (AppointmentSlot slot : slots) {
            if (!slot.isBooked() && !slot.isCancelled()) {
                available.add(slot);
            }
        }
        return available;
    }

    /**
     * Books the slot with the given time. Returns true if the slot was found
     * and successfully booked, false if it does not exist, is already booked, or is cancelled.
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
            if (slot.getTime().equals(normalizedTime) && !slot.isBooked() && !slot.isCancelled()) {
                slot.book();

                eventManager.notifyObservers("Appointment booked successfully at " + normalizedTime);

                return true;
            }
        }

        return false;
    }

    /**
     * Adds a new appointment slot at the specified time.
     * This method does not enforce authorization - use AppointmentBookingService.addManagedSlot() for that.
     *
     * @param time slot time identifier
     * @return true if the slot was added successfully, false if it already exists or time is invalid
     */
    public boolean addSlot(String time) {
        if (time == null || time.trim().isEmpty()) {
            return false;
        }

        String normalizedTime = time.trim();
        
        // Check if slot already exists (including cancelled slots)
        for (AppointmentSlot slot : slots) {
            if (slot.getTime().equals(normalizedTime)) {
                return false;
            }
        }

        AppointmentSlot newSlot = new AppointmentSlot(normalizedTime);
        boolean saved = appointmentRepository.save(newSlot);
        if (saved) {
            slots.add(newSlot);
            eventManager.notifyObservers("New appointment slot added: " + normalizedTime);
            return true;
        }
        return false;
    }

    /**
     * Cancels (removes) an appointment slot at the specified time.
     * This method does not enforce authorization - use AppointmentBookingService.cancelManagedSlot() for that.
     *
     * @param time slot time identifier
     * @return true if the slot was cancelled successfully, false if it does not exist or is already cancelled
     */
    public boolean cancelSlot(String time) {
        if (time == null || time.trim().isEmpty()) {
            return false;
        }

        String normalizedTime = time.trim();
        
        boolean removed = appointmentRepository.removeSlot(normalizedTime);
        if (removed) {
            // Update in-memory list
            for (AppointmentSlot slot : slots) {
                if (slot.getTime().equals(normalizedTime)) {
                    slot.cancel();
                    break;
                }
            }
            eventManager.notifyObservers("Appointment slot cancelled: " + normalizedTime);
            return true;
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
            if (slot.getTime().equals(normalizedTime) && slot.isBooked() && !slot.isCancelled()) {
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
            if (slot.isBooked() && !slot.isCancelled()) {
                eventManager.notifyObservers("Reminder: Appointment at " + slot.getTime());
                sentCount++;
            }
        }
        return sentCount;
    }
}

