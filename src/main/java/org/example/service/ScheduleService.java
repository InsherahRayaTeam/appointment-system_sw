package org.example.service;

import org.example.domain.AppointmentSlot;
import org.example.domain.TimeSlotStatus;
import org.example.domain.UserRole;
import org.example.repository.AppointmentRepository;
import org.example.notification.SlotCancelledEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Manages the lifecycle of appointment slots with role-based access control.
 * This service is responsible for adding, cancelling, and listing slots.
 *
 * ADMIN-only operations:
 * - Create new time slots
 * - Cancel existing slots
 *
 * USER-facing operations:
 * - View available slots
 *
 * @author appointment-system
 * @version 1.0
 */
public class ScheduleService {

    private final AppointmentRepository appointmentRepository;
    private final SessionManager sessionManager;
    private final EventManager eventManager;
    private final List<AppointmentSlot> slots = new ArrayList<>();

    /**
     * Creates a schedule service.
     *
     * @param appointmentRepository repository for slot persistence
     * @param sessionManager session manager for role-based access control
     * @param eventManager event manager for publishing notifications
     */
    public ScheduleService(
            AppointmentRepository appointmentRepository,
            SessionManager sessionManager,
            EventManager eventManager
    ) {
        this.appointmentRepository = Objects.requireNonNull(
                appointmentRepository,
                "appointmentRepository cannot be null"
        );
        this.sessionManager = Objects.requireNonNull(sessionManager, "sessionManager cannot be null");
        this.eventManager = Objects.requireNonNull(eventManager, "eventManager cannot be null");
        this.slots.addAll(appointmentRepository.findAll());
    }

    /**
     * Lists all available slots (no role restriction - accessible to all authenticated users).
     *
     * @return list of slots that are available and not cancelled
     * @throws IllegalStateException if user is not logged in
     */
    public List<AppointmentSlot> listAvailableSlots() {
        if (!sessionManager.isLoggedIn()) {
            throw new IllegalStateException("user must be logged in to view available slots");
        }

        List<AppointmentSlot> available = new ArrayList<>();
        for (AppointmentSlot slot : slots) {
            if (slot.isAvailable() && !slot.isCancelled()) {
                available.add(slot);
            }
        }
        return available;
    }

    /**
     * Lists all slots (both available and booked, excluding cancelled).
     * Accessible to all authenticated users.
     *
     * @return list of all non-cancelled slots
     * @throws IllegalStateException if user is not logged in
     */
    public List<AppointmentSlot> listAllSlots() {
        if (!sessionManager.isLoggedIn()) {
            throw new IllegalStateException("user must be logged in to view slots");
        }

        List<AppointmentSlot> all = new ArrayList<>();
        for (AppointmentSlot slot : slots) {
            if (!slot.isCancelled()) {
                all.add(slot);
            }
        }
        return all;
    }

    /**
     * Adds a new time slot. Requires ADMIN role.
     *
     * @param time the time label for the slot
     * @return true if slot was added successfully
     * @throws IllegalStateException if user is not logged in or not an ADMIN
     */
    public boolean addSlot(String time) {
        if (!sessionManager.isLoggedIn()) {
            throw new IllegalStateException("user must be logged in to manage slots");
        }
        if (!sessionManager.isAdmin()) {
            throw new IllegalStateException("only ADMIN role can create slots");
        }
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
            eventManager.notifyObservers("New slot created: " + normalizedTime);
        }
        return saved;
    }

    /**
     * Cancels an existing slot. Requires ADMIN role.
     * Cancelling a slot prevents any future bookings on that slot.
     *
     * @param time the time label of the slot to cancel
     * @return true if slot was cancelled successfully
     * @throws IllegalStateException if user is not logged in or not an ADMIN
     */
    public boolean cancelSlot(String time) {
        if (!sessionManager.isLoggedIn()) {
            throw new IllegalStateException("user must be logged in to manage slots");
        }
        if (!sessionManager.isAdmin()) {
            throw new IllegalStateException("only ADMIN role can cancel slots");
        }
        if (time == null || time.trim().isEmpty()) {
            return false;
        }

        String normalizedTime = time.trim();

        for (AppointmentSlot slot : slots) {
            if (slot.getTime().equals(normalizedTime) && !slot.isCancelled()) {
                slot.cancel();
                appointmentRepository.removeSlot(normalizedTime);

                // Publish slot cancellation event
                String adminUsername = sessionManager.getCurrentUsername();
                SlotCancelledEvent event = new SlotCancelledEvent(normalizedTime, adminUsername);
                eventManager.notifyObservers(
                        "Slot cancelled by admin: " + normalizedTime + " (admin: " + adminUsername + ")"
                );

                return true;
            }
        }

        return false;
    }

    /**
     * Gets the total number of slots currently loaded.
     *
     * @return count of all slots
     */
    public int getTotalSlotCount() {
        return slots.size();
    }

    /**
     * Gets the number of available slots.
     *
     * @return count of available, non-cancelled slots
     */
    public int getAvailableSlotCount() {
        int count = 0;
        for (AppointmentSlot slot : slots) {
            if (slot.isAvailable() && !slot.isCancelled()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gets the number of booked slots.
     *
     * @return count of booked slots
     */
    public int getBookedSlotCount() {
        int count = 0;
        for (AppointmentSlot slot : slots) {
            if (slot.isBooked()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gets the number of cancelled slots.
     *
     * @return count of cancelled slots
     */
    public int getCancelledSlotCount() {
        int count = 0;
        for (AppointmentSlot slot : slots) {
            if (slot.isCancelled()) {
                count++;
            }
        }
        return count;
    }
}

