package org.example.service;

import org.example.domain.AppointmentSlot;
import org.example.repository.AppointmentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents appointment service in the system.
 */
public class AppointmentService {

    private final List<AppointmentSlot> slots = new ArrayList<>();
    private final EventManager eventManager;


    /**
     * Creates a new appointment service object with the given values.
     *
     * @param appointmentRepository repository used to read and save data
     * @param eventManager manager object used for shared app state
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
     * Returns the available slots.
     *
     * @return collection with the requested results
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
     * Books slot when allowed.
     *
     * @param time time value used by this method
     *
     * @return true when the action is valid or successful, otherwise false
     */
    public boolean bookSlot(String time) {
        if (time == null || time.trim().isEmpty()) {
            return false;
        }

        String normalizedTime = time.trim();

        for (AppointmentSlot slot : slots) {
            if (slot.matchesSelection(normalizedTime) && !slot.isBooked()) {
                slot.book();

                eventManager.notifyObservers("Appointment booked successfully at " + slot.getDateDayTimeLabel());

                return true;
            }
        }

        return false;
    }

    /**
     * Runs send reminder for this class.
     *
     * @param time time value used by this method
     */
    public void sendReminder(String time) {
        sendReminderForSlot(time);
    }

    /**
     * Runs send reminder for slot for this class.
     *
     * @param time time value used by this method
     *
     * @return true when the action is valid or successful, otherwise false
     */
    public boolean sendReminderForSlot(String time) {
        if (time == null || time.trim().isEmpty()) {
            return false;
        }

        String normalizedTime = time.trim();
        for (AppointmentSlot slot : slots) {
            if (slot.matchesSelection(normalizedTime) && slot.isBooked()) {
                eventManager.notifyObservers("Reminder: Appointment at " + slot.getDateDayTimeLabel());
                return true;
            }
        }

        return false;
    }

    /**
     * Runs send all reminders for this class.
     *
     * @return numeric result from this method
     */
    public int sendAllReminders() {
        int sentCount = 0;
        for (AppointmentSlot slot : slots) {
            if (slot.isBooked()) {
                eventManager.notifyObservers("Reminder: Appointment at " + slot.getDateDayTimeLabel());
                sentCount++;
            }
        }
        return sentCount;
    }
}
