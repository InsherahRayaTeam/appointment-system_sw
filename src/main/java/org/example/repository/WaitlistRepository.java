package org.example.repository;

import org.example.domain.WaitlistEntry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Defines the operations for waitlist persistence.
 */
public interface WaitlistRepository {

    /**
     * Saves a waitlist entry.
     *
     * @param entry value for waitlist entry
     */
    void save(WaitlistEntry entry);

    /**
     * Restores a waitlist entry to the front of the queue.
     *
     * @param entry value for waitlist entry
     */
    void saveFirst(WaitlistEntry entry);

    /**
     * Returns all waitlist entries.
     *
     * @return collection with the requested results
     */
    List<WaitlistEntry> findAll();

    /**
     * Returns the waitlist entries for a slot.
     *
     * @param slotDateTime date/time used to find matching entries
     * @return collection with the requested results
     */
    List<WaitlistEntry> findBySlotDateTime(LocalDateTime slotDateTime);

    /**
     * Removes and returns the first entry for a slot.
     *
     * @param slotDateTime date/time used to find matching entries
     * @return optional value if data is found
     */
    Optional<WaitlistEntry> pollFirstBySlotDateTime(LocalDateTime slotDateTime);

    /**
     * Checks whether a customer already exists on the waitlist for a slot.
     *
     * @param slotDateTime date/time used to find matching entries
     * @param customerName value for customer name
     * @param customerEmail value for customer email
     * @return true when the customer is already present, otherwise false
     */
    boolean existsForSlotAndCustomer(LocalDateTime slotDateTime, String customerName, String customerEmail);
}

