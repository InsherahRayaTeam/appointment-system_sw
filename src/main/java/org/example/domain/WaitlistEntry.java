package org.example.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a waitlist entry for a specific appointment slot.
 */
public class WaitlistEntry {

    private final String id;
    private final String customerName;
    private final String customerEmail;
    private final String customerPhoneNumber;
    private final LocalDateTime slotDateTime;
    private final int durationMinutes;
    private final int participantCount;
    private final AppointmentType type;
    private final LocalDateTime createdAt;

    /**
     * Creates a new waitlist entry object with the given values.
     *
     * @param id unique id used to find the record
     * @param customerName value for customer name
     * @param customerEmail value for customer email
     * @param customerPhoneNumber value for customer phone number
     * @param slotDateTime date/time for the target slot
     * @param durationMinutes appointment duration in minutes
     * @param participantCount number of people for the appointment
     * @param type value for appointment type
     * @param createdAt time used to preserve FIFO order
     */
    public WaitlistEntry(
            String id,
            String customerName,
            String customerEmail,
            String customerPhoneNumber,
            LocalDateTime slotDateTime,
            int durationMinutes,
            int participantCount,
            AppointmentType type,
            LocalDateTime createdAt
    ) {
        this.id = id == null || id.trim().isEmpty() ? UUID.randomUUID().toString() : id.trim();
        this.customerName = normalize(customerName);
        this.customerEmail = normalize(customerEmail);
        this.customerPhoneNumber = normalize(customerPhoneNumber);
        this.slotDateTime = slotDateTime;
        this.durationMinutes = durationMinutes;
        this.participantCount = participantCount;
        this.type = type == null ? AppointmentType.NORMAL : type;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    /**
     * Creates a new waitlist entry object with the given values.
     *
     * @param customerName value for customer name
     * @param customerEmail value for customer email
     * @param customerPhoneNumber value for customer phone number
     * @param slotDateTime date/time for the target slot
     * @param durationMinutes appointment duration in minutes
     * @param participantCount number of people for the appointment
     * @param type value for appointment type
     */
    public WaitlistEntry(
            String customerName,
            String customerEmail,
            String customerPhoneNumber,
            LocalDateTime slotDateTime,
            int durationMinutes,
            int participantCount,
            AppointmentType type
    ) {
        this(
                UUID.randomUUID().toString(),
                customerName,
                customerEmail,
                customerPhoneNumber,
                slotDateTime,
                durationMinutes,
                participantCount,
                type,
                LocalDateTime.now()
        );
    }

    /**
     * Returns the id.
     *
     * @return unique identifier for this entry
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the customer name.
     *
     * @return customer name associated with this entry
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Returns the customer email.
     *
     * @return customer email associated with this entry
     */
    public String getCustomerEmail() {
        return customerEmail;
    }

    /**
     * Returns the customer phone number.
     *
     * @return customer phone number associated with this entry
     */
    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }

    /**
     * Returns the slot date/time.
     *
     * @return date/time for the target slot
     */
    public LocalDateTime getSlotDateTime() {
        return slotDateTime;
    }

    /**
     * Returns the duration minutes.
     *
     * @return appointment duration in minutes
     */
    public int getDurationMinutes() {
        return durationMinutes;
    }

    /**
     * Returns the participant count.
     *
     * @return number of participants requested
     */
    public int getParticipantCount() {
        return participantCount;
    }

    /**
     * Returns the appointment type.
     *
     * @return appointment type associated with this entry
     */
    public AppointmentType getType() {
        return type;
    }

    /**
     * Returns the timestamp when the entry was created.
     *
     * @return creation timestamp used for FIFO ordering
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Checks whether this entry matches a customer identity.
     *
     * @param candidateName value for customer name
     * @param candidateEmail value for customer email
     * @return true when the identity matches this entry, otherwise false
     */
    public boolean matchesCustomer(String candidateName, String candidateEmail) {
        String candidateIdentity = customerIdentity(candidateName, candidateEmail);
        return Objects.equals(customerIdentity(customerName, customerEmail), candidateIdentity);
    }

    /**
     * Converts this waitlist entry into a confirmed appointment.
     *
     * @return appointment built from this waitlist entry
     */
    public Appointment toAppointment() {
        return toAppointment(AppointmentStatus.CONFIRMED);
    }

    /**
     * Converts this waitlist entry into an appointment with the requested status.
     *
     * @param status value for appointment status
     * @return appointment built from this waitlist entry
     */
    public Appointment toAppointment(AppointmentStatus status) {
        AppointmentDetails details = new AppointmentDetails(
                customerName,
                customerEmail,
                customerPhoneNumber,
                slotDateTime,
                durationMinutes,
                participantCount
        );

        return new Appointment(
                id,
                details,
                status == null ? AppointmentStatus.CONFIRMED : status,
                type == null ? AppointmentType.NORMAL : type
        );
    }

    /**
     * Normalizes text values.
     *
     * @param value text value
     * @return trimmed value, or null when blank
     */
    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    /**
     * Builds a stable customer identity from email or name.
     *
     * @param name customer name
     * @param email customer email
     * @return normalized identity
     */
    private String customerIdentity(String name, String email) {
        String normalizedEmail = normalize(email);
        if (normalizedEmail != null) {
            return normalizedEmail.toLowerCase();
        }

        String normalizedName = normalize(name);
        return normalizedName == null ? null : normalizedName.toLowerCase();
    }
}