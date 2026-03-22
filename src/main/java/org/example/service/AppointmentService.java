package org.example.service;

import org.example.domain.Appointment;
import org.example.domain.AppointmentSlot;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {

    private static final DateTimeFormatter SLOT_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final List<AppointmentSlot> slots = new ArrayList<>();
    private final List<BookingRuleStrategy> rules = new ArrayList<>();

    public AppointmentService() {

        this(List.of(new DurationRule(), new ParticipantRule()));
    }

    public AppointmentService(List<BookingRuleStrategy> bookingRules) {

        slots.add(new AppointmentSlot("10:00"));
        slots.add(new AppointmentSlot("11:00"));
        slots.add(new AppointmentSlot("12:00"));

        if (bookingRules != null) {
            rules.addAll(bookingRules);
        }
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
    public boolean bookAppointment(Appointment appointment) {

        if (appointment == null || appointment.getStartTime() == null) return false;

        if (!validateAppointmentByRules(appointment)) return false;

        // Format appointment time to match slot format (HH:mm)
        String appointmentTime = appointment.getStartTime().toLocalTime()
                .format(SLOT_TIME_FORMATTER);

        // Book the appointment slot if available
        for (AppointmentSlot slot : slots) {
            if (slot.getTime().equals(appointmentTime) && !slot.isBooked()) {
                slot.book();
                return true;
            }
        }

        return false;
    }

    private boolean validateAppointmentByRules(Appointment appointment) {
        for (BookingRuleStrategy rule : rules) {
            if (!rule.isValid(appointment)) {
                return false;
            }
        }
        return true;
    }
}