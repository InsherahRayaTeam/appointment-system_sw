package org.example.service;

import org.example.domain.AppointmentSlot;
import org.example.repository.AppointmentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents appointment service in the system.
 */
public class AppointmentService {

    private static final String BOOKED_SUCCESS_PREFIX = "Appointment booked successfully at ";
    private static final String REMINDER_PREFIX = "Reminder: Appointment at ";

    private final AppointmentRepository appointmentRepository;
    private final EventManager eventManager;


    /**
     * Creates a new appointment service object with the given values.
     *
     * @param appointmentRepository repository used to read and save data
     * @param eventManager manager object used for shared app state
     */
    public AppointmentService(AppointmentRepository appointmentRepository, EventManager eventManager) {
        this.appointmentRepository = Objects.requireNonNull(
                appointmentRepository,
                "appointmentRepository cannot be null"
        );
        this.eventManager = Objects.requireNonNull(eventManager, "eventManager cannot be null");
    }

    /**
     * Returns the available slots.
     *
     * @return collection with the requested results
     */
    public List<AppointmentSlot> getAvailableSlots() {
        List<AppointmentSlot> available = new ArrayList<>();
        for (AppointmentSlot slot : appointmentRepository.findAll()) {
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

        for (AppointmentSlot slot : appointmentRepository.findAll()) {
            if (slot.matchesSelection(normalizedTime) && !slot.isBooked()) {
                slot.book();

                eventManager.notifyObservers(BOOKED_SUCCESS_PREFIX + slot.getDateDayTimeLabel());

                return true;
            }
        }

        return false;
    }

    /**
     * Adds a future slot when the date/time input is valid and unique.
     *
     * @param dateText slot date text in yyyy-MM-dd format
     * @param timeText slot time text in HH:mm format
     * @return status that explains the operation result
     */
    public BookingStatus addSlot(String dateText, String timeText) {
        if (dateText == null || dateText.trim().isEmpty()) {
            return BookingStatus.INVALID_SLOT_DATE_TIME;
        }
        if (timeText == null || timeText.trim().isEmpty()) {
            return BookingStatus.BLANK_SLOT_TIME;
        }

        LocalDate date;
        LocalTime time;
        try {
            date = LocalDate.parse(dateText.trim());
            time = LocalTime.parse(timeText.trim());
        } catch (RuntimeException ex) {
            return BookingStatus.INVALID_SLOT_DATE_TIME;
        }

        return addSlot(date, time);
    }

    /**
     * Adds a future slot when the values are valid and unique.
     *
     * @param date slot date
     * @param time slot time
     * @return status that explains the operation result
     */
    public BookingStatus addSlot(LocalDate date, LocalTime time) {
        if (date == null || time == null) {
            return BookingStatus.INVALID_SLOT_DATE_TIME;
        }

        LocalDateTime slotDateTime = date.atTime(time);
        if (!slotDateTime.isAfter(LocalDateTime.now())) {
            return BookingStatus.INVALID_SLOT_DATE_TIME;
        }

        for (AppointmentSlot existing : appointmentRepository.findAll()) {
            if (slotDateTime.equals(existing.getDateTime())) {
                return BookingStatus.DUPLICATE_SLOT;
            }
        }

        boolean added = appointmentRepository.addSlot(new AppointmentSlot(date, time));
        if (!added) {
            return BookingStatus.DUPLICATE_SLOT;
        }

        eventManager.notifyObservers("Appointment slot added: " + slotDateTime);
        return BookingStatus.SUCCESS;
    }

    /**
     * Sends a reminder for one booked slot that matches the given selection.
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
        for (AppointmentSlot slot : appointmentRepository.findAll()) {
            if (slot.matchesSelection(normalizedTime) && slot.isBooked()) {
                eventManager.notifyObservers(REMINDER_PREFIX + slot.getDateDayTimeLabel());
                return true;
            }
        }

        return false;
    }

    /**
     * Sends reminders for all booked slots.
     *
     * @return numeric result from this method
     */
    public int sendAllReminders() {
        int sentCount = 0;
        for (AppointmentSlot slot : appointmentRepository.findAll()) {
            if (slot.isBooked()) {
                eventManager.notifyObservers(REMINDER_PREFIX + slot.getDateDayTimeLabel());
                sentCount++;
            }
        }
        return sentCount;
    }

}
