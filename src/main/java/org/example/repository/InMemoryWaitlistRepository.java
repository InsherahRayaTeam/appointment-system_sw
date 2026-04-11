package org.example.repository;

import org.example.domain.WaitlistEntry;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents in-memory waitlist persistence in the system.
 */
public class InMemoryWaitlistRepository implements WaitlistRepository {

    private final List<WaitlistEntry> entries = new ArrayList<>();

    /**
     * Saves a waitlist entry.
     *
     * @param entry value for waitlist entry
     */
    @Override
    public void save(WaitlistEntry entry) {
        if (entry == null) {
            throw new NullPointerException("entry cannot be null");
        }

        entries.add(copyOf(entry));
    }

    /**
     * Restores a waitlist entry to the front of the queue.
     *
     * @param entry value for waitlist entry
     */
    @Override
    public void saveFirst(WaitlistEntry entry) {
        if (entry == null) {
            throw new NullPointerException("entry cannot be null");
        }

        entries.add(0, copyOf(entry));
    }

    /**
     * Returns all waitlist entries.
     *
     * @return collection with the requested results
     */
    @Override
    public List<WaitlistEntry> findAll() {
        List<WaitlistEntry> copies = new ArrayList<>();
        for (WaitlistEntry entry : entries) {
            copies.add(copyOf(entry));
        }
        return copies;
    }

    /**
     * Returns the waitlist entries for a slot.
     *
     * @param slotDateTime date/time used to find matching entries
     * @return collection with the requested results
     */
    @Override
    public List<WaitlistEntry> findBySlotDateTime(LocalDateTime slotDateTime) {
        List<WaitlistEntry> copies = new ArrayList<>();
        if (slotDateTime == null) {
            return copies;
        }

        for (WaitlistEntry entry : entries) {
            if (slotDateTime.equals(entry.getSlotDateTime())) {
                copies.add(copyOf(entry));
            }
        }
        return copies;
    }

    /**
     * Removes and returns the first entry for a slot.
     *
     * @param slotDateTime date/time used to find matching entries
     * @return optional value if data is found
     */
    @Override
    public Optional<WaitlistEntry> pollFirstBySlotDateTime(LocalDateTime slotDateTime) {
        if (slotDateTime == null) {
            return Optional.empty();
        }

        for (int i = 0; i < entries.size(); i++) {
            WaitlistEntry entry = entries.get(i);
            if (slotDateTime.equals(entry.getSlotDateTime())) {
                entries.remove(i);
                return Optional.of(copyOf(entry));
            }
        }

        return Optional.empty();
    }

    /**
     * Checks whether a customer already exists on the waitlist for a slot.
     *
     * @param slotDateTime date/time used to find matching entries
     * @param customerName value for customer name
     * @param customerEmail value for customer email
     * @return true when the customer is already present, otherwise false
     */
    @Override
    public boolean existsForSlotAndCustomer(LocalDateTime slotDateTime, String customerName, String customerEmail) {
        if (slotDateTime == null) {
            return false;
        }

        for (WaitlistEntry entry : entries) {
            if (slotDateTime.equals(entry.getSlotDateTime()) && entry.matchesCustomer(customerName, customerEmail)) {
                return true;
            }
        }
        return false;
    }

    private WaitlistEntry copyOf(WaitlistEntry entry) {
        return new WaitlistEntry(
                entry.getId(),
                entry.getCustomerName(),
                entry.getCustomerEmail(),
                entry.getCustomerPhoneNumber(),
                entry.getSlotDateTime(),
                entry.getDurationMinutes(),
                entry.getParticipantCount(),
                entry.getType(),
                entry.getCreatedAt()
        );
    }
}

